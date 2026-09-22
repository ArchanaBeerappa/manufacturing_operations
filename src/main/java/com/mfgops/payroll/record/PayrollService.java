package com.mfgops.payroll.record;

import com.mfgops.common.AuditLogService;
import com.mfgops.common.exception.ResourceNotFoundException;
import com.mfgops.workforce.employee.Employee;
import com.mfgops.workforce.employee.EmployeeRepository;
import com.mfgops.workforce.attendance.Attendance;
import com.mfgops.workforce.attendance.AttendanceRepository;
import com.mfgops.payroll.advance.AdvanceSalary;
import com.mfgops.payroll.advance.AdvanceSalaryRepository;
import com.mfgops.payroll.record.dto.PayrollCalculationRequest;
import com.mfgops.payroll.record.dto.PayrollResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRecordRepository payrollRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final AdvanceSalaryRepository advanceSalaryRepository;
    private final AttendanceRepository attendanceRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public PayrollResponse calculatePayroll(PayrollCalculationRequest request) {
        // 1. Resolve and Validate Employee
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + request.getEmployeeId()));

        String month = request.getPayrollMonth();
        YearMonth ym = YearMonth.parse(month); // E.g., YYYY-MM
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        // 2. Fetch Attendance Log Ranges
        List<Attendance> attendances = attendanceRepository.findByEmployeeIdAndDateBetween(request.getEmployeeId(), start, end);

        // 3. Perform Arithmetic Calculations
        BigDecimal baseSalary = BigDecimal.ZERO;
        BigDecimal dailyWageEarnings = BigDecimal.ZERO;
        BigDecimal overtimeEarnings = BigDecimal.ZERO; // For portfolio expansion
        BigDecimal otherDeductions = BigDecimal.ZERO;

        if ("FIXED_MONTHLY".equalsIgnoreCase(employee.getSalaryType())) {
            baseSalary = employee.getMonthlySalaryRate();
            int totalDaysInMonth = ym.lengthOfMonth();
            BigDecimal basePerDay = baseSalary.divide(BigDecimal.valueOf(totalDaysInMonth), 4, RoundingMode.HALF_UP);

            // Deduct salary per absent day or half-day
            long absentCount = attendances.stream().filter(a -> "ABSENT".equals(a.getStatus())).count();
            long halfDayCount = attendances.stream().filter(a -> "HALF_DAY".equals(a.getStatus())).count();

            BigDecimal absentDeduction = basePerDay.multiply(BigDecimal.valueOf(absentCount));
            BigDecimal halfDayDeduction = basePerDay.multiply(BigDecimal.valueOf(halfDayCount)).multiply(BigDecimal.valueOf(0.5));
            
            otherDeductions = absentDeduction.add(halfDayDeduction).setScale(2, RoundingMode.HALF_UP);
        } else if ("DAILY_WAGE".equalsIgnoreCase(employee.getSalaryType())) {
            BigDecimal rate = employee.getDailyWageRate();

            long presents = attendances.stream().filter(a -> "PRESENT".equals(a.getStatus())).count();
            long lates = attendances.stream().filter(a -> "LATE".equals(a.getStatus())).count();
            long halfDays = attendances.stream().filter(a -> "HALF_DAY".equals(a.getStatus())).count();

            // Daily wages earn full wage on Presents and Lates, and half wage on Half-Days
            BigDecimal fullDaysWage = rate.multiply(BigDecimal.valueOf(presents + lates));
            BigDecimal halfDaysWage = rate.multiply(BigDecimal.valueOf(halfDays)).multiply(BigDecimal.valueOf(0.5));

            dailyWageEarnings = fullDaysWage.add(halfDaysWage).setScale(2, RoundingMode.HALF_UP);
        } else {
            throw new IllegalArgumentException("Unsupported employee salary type: " + employee.getSalaryType());
        }

        // 4. Resolve Approved Advance Loan Deductions
        List<AdvanceSalary> approvedAdvances = advanceSalaryRepository
                .findByEmployeeIdAndDeductionMonthAndStatus(request.getEmployeeId(), month, "APPROVED");

        BigDecimal advanceDeductions = approvedAdvances.stream()
                .map(AdvanceSalary::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Calculate Final Net Earnings
        BigDecimal grossEarnings = "FIXED_MONTHLY".equalsIgnoreCase(employee.getSalaryType()) ? baseSalary : dailyWageEarnings;
        BigDecimal deductions = advanceDeductions.add(otherDeductions);
        BigDecimal netSalary = grossEarnings.subtract(deductions);

        if (netSalary.compareTo(BigDecimal.ZERO) < 0) {
            netSalary = BigDecimal.ZERO;
        }

        // 6. Save or Update Record (Prevent double payments)
        PayrollRecord record = payrollRecordRepository
                .findByEmployeeIdAndPayrollMonth(request.getEmployeeId(), month)
                .orElse(new PayrollRecord());

        if (record.getId() != null && "PAID".equalsIgnoreCase(record.getStatus())) {
            throw new IllegalStateException("Payroll record has already been paid for this month: " + month);
        }

        // Snapshot old state for auditing
        PayrollRecord oldState = null;
        if (record.getId() != null) {
            oldState = new PayrollRecord();
            oldState.setId(record.getId());
            oldState.setTenantId(record.getTenantId());
            oldState.setStatus(record.getStatus());
            oldState.setNetSalary(record.getNetSalary());
        }

        record.setEmployee(employee);
        record.setPayrollMonth(month);
        record.setBaseSalary(baseSalary);
        record.setDailyWageEarnings(dailyWageEarnings);
        record.setOvertimeEarnings(overtimeEarnings);
        record.setAdvanceDeductions(advanceDeductions);
        record.setOtherDeductions(otherDeductions);
        record.setNetSalary(netSalary);
        record.setStatus("DRAFT");

        PayrollRecord saved = payrollRecordRepository.save(record);

        // Explicit Auditing
        String action = (oldState == null) ? "CREATE" : "UPDATE";
        auditLogService.log(saved.getTenantId(), "PayrollRecord", saved.getId(), action, oldState, saved);

        return PayrollResponse.fromEntity(saved);
    }

    @Transactional
    public PayrollResponse approvePayroll(UUID id) {
        PayrollRecord record = payrollRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found: " + id));

        if (!"DRAFT".equalsIgnoreCase(record.getStatus())) {
            throw new IllegalStateException("Only DRAFT payroll records can be approved. Current status: " + record.getStatus());
        }

        // Snapshot old state
        PayrollRecord oldState = new PayrollRecord();
        oldState.setId(record.getId());
        oldState.setTenantId(record.getTenantId());
        oldState.setStatus(record.getStatus());

        record.setStatus("APPROVED");
        PayrollRecord saved = payrollRecordRepository.save(record);

        // Explicit Auditing
        auditLogService.log(saved.getTenantId(), "PayrollRecord", saved.getId(), "APPROVE", oldState, saved);

        return PayrollResponse.fromEntity(saved);
    }

    @Transactional
    public PayrollResponse paySalary(UUID id) {
        PayrollRecord record = payrollRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found: " + id));

        if (!"APPROVED".equalsIgnoreCase(record.getStatus())) {
            throw new IllegalStateException("Only APPROVED payroll records can be paid. Current status: " + record.getStatus());
        }

        // Snapshot old state
        PayrollRecord oldState = new PayrollRecord();
        oldState.setId(record.getId());
        oldState.setTenantId(record.getTenantId());
        oldState.setStatus(record.getStatus());

        // Perform State Transition
        record.setStatus("PAID");
        record.setPaidAt(LocalDateTime.now());
        PayrollRecord saved = payrollRecordRepository.save(record);

        // Transition linked approved advance loans to DEDUCTED
        List<AdvanceSalary> approvedAdvances = advanceSalaryRepository
                .findByEmployeeIdAndDeductionMonthAndStatus(record.getEmployee().getId(), record.getPayrollMonth(), "APPROVED");
        
        for (AdvanceSalary adv : approvedAdvances) {
            adv.setStatus("DEDUCTED");
            advanceSalaryRepository.save(adv);
        }

        // Explicit Auditing
        auditLogService.log(saved.getTenantId(), "PayrollRecord", saved.getId(), "PAY_DISBURSE", oldState, saved);

        return PayrollResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<PayrollResponse> getAllPayrollRecords(Pageable pageable) {
        return payrollRecordRepository.findAll(pageable)
                .map(PayrollResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<PayrollResponse> getPayrollByEmployeeId(UUID employeeId, Pageable pageable) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found: " + employeeId);
        }
        return payrollRecordRepository.findByEmployeeId(employeeId, pageable)
                .map(PayrollResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public PayrollRecord getPayrollRecordEntityById(UUID id) {
        return payrollRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found: " + id));
    }
}
