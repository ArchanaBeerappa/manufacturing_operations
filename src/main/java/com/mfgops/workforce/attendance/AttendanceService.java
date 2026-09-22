package com.mfgops.workforce.attendance;

import com.mfgops.auth.User;
import com.mfgops.auth.UserRepository;
import com.mfgops.common.AuditLogService;
import com.mfgops.common.exception.ResourceNotFoundException;
import com.mfgops.workforce.employee.Employee;
import com.mfgops.workforce.employee.EmployeeRepository;
import com.mfgops.workforce.shift.Shift;
import com.mfgops.workforce.shift.ShiftRepository;
import com.mfgops.workforce.attendance.dto.AttendanceRequest;
import com.mfgops.workforce.attendance.dto.AttendanceResponse;
import com.mfgops.workforce.attendance.dto.AttendanceCorrectionRequest;
import com.mfgops.workforce.attendance.dto.MonthlySummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public AttendanceResponse checkIn(AttendanceRequest request) {
        // 1. Resolve and Validate Employee
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + request.getEmployeeId()));

        if (!"ACTIVE".equalsIgnoreCase(employee.getStatus())) {
            throw new IllegalStateException("Employee is not active. Cannot perform check-in.");
        }

        // 2. Prevent Multiple Open Check-Ins
        attendanceRepository.findByEmployeeIdAndCheckOutTimeIsNull(request.getEmployeeId())
                .ifPresent(att -> {
                    throw new IllegalStateException("Employee already has an open check-in session from date: " + att.getDate());
                });

        LocalDate today = LocalDate.now();
        
        // 3. Prevent Duplicate Punch for Same Calendar Date
        if (attendanceRepository.findByEmployeeIdAndDate(request.getEmployeeId(), today).isPresent()) {
            throw new IllegalStateException("Employee already has a recorded attendance punch for today: " + today);
        }

        // 4. Resolve Shift
        Shift shift = shiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + request.getShiftId()));

        // 5. Evaluate Lateness (e.g. 15 minute grace period)
        LocalDateTime checkInTime = LocalDateTime.now();
        LocalTime checkInTimeOnly = checkInTime.toLocalTime();
        LocalTime shiftStart = shift.getStartTime();
        String status = "PRESENT";

        if (checkInTimeOnly.isAfter(shiftStart.plusMinutes(15))) {
            status = "LATE";
        }

        // 6. Persist Attendance
        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(today);
        attendance.setCheckInTime(checkInTime);
        attendance.setShift(shift);
        attendance.setStatus(status);

        Attendance saved = attendanceRepository.save(attendance);

        // Explicit Auditing
        auditLogService.log(saved.getTenantId(), "Attendance", saved.getId(), "CREATE", null, saved);

        return AttendanceResponse.fromEntity(saved);
    }

    @Transactional
    public AttendanceResponse checkOut(UUID employeeId) {
        // Resolve Active Session
        Attendance attendance = attendanceRepository.findByEmployeeIdAndCheckOutTimeIsNull(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("No active check-in session found for employee: " + employeeId));

        // Snapshot state for auditing
        Attendance oldState = new Attendance();
        oldState.setId(attendance.getId());
        oldState.setTenantId(attendance.getTenantId());
        oldState.setEmployee(attendance.getEmployee());
        oldState.setDate(attendance.getDate());
        oldState.setCheckInTime(attendance.getCheckInTime());
        oldState.setStatus(attendance.getStatus());
        oldState.setShift(attendance.getShift());

        LocalDateTime checkOutTime = LocalDateTime.now();
        attendance.setCheckOutTime(checkOutTime);

        // Calculate hours worked to verify Half-Day status (e.g. less than 4 hours = HALF_DAY)
        java.time.Duration duration = java.time.Duration.between(attendance.getCheckInTime(), checkOutTime);
        long hours = duration.toHours();
        if (hours < 4 && !"LATE".equals(attendance.getStatus())) {
            attendance.setStatus("HALF_DAY");
        }

        Attendance saved = attendanceRepository.save(attendance);

        // Explicit Auditing
        auditLogService.log(saved.getTenantId(), "Attendance", saved.getId(), "CHECK_OUT", oldState, saved);

        return AttendanceResponse.fromEntity(saved);
    }

    @Transactional
    public AttendanceResponse correctAttendance(UUID id, AttendanceCorrectionRequest request) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found: " + id));

        // Fetch Supervisor Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User supervisor = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            supervisor = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("Supervisor context not found: " + authentication.getName()));
        }

        // Snapshot old state for auditing
        Attendance oldState = new Attendance();
        oldState.setId(attendance.getId());
        oldState.setTenantId(attendance.getTenantId());
        oldState.setEmployee(attendance.getEmployee());
        oldState.setDate(attendance.getDate());
        oldState.setCheckInTime(attendance.getCheckInTime());
        oldState.setCheckOutTime(attendance.getCheckOutTime());
        oldState.setStatus(attendance.getStatus());
        oldState.setShift(attendance.getShift());

        // Apply manual correction overrides
        attendance.setCheckInTime(request.getCheckInTime());
        attendance.setCheckOutTime(request.getCheckOutTime());
        attendance.setStatus(request.getStatus().toUpperCase());
        attendance.setCorrectedBy(supervisor);
        attendance.setCorrectionReason(request.getReason());
        attendance.setCorrectedAt(LocalDateTime.now());

        Attendance saved = attendanceRepository.save(attendance);

        // Explicit Dual Auditing
        auditLogService.log(saved.getTenantId(), "Attendance", saved.getId(), "MANUAL_CORRECTION", oldState, saved);

        return AttendanceResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getAllAttendances(Pageable pageable) {
        return attendanceRepository.findAll(pageable)
                .map(AttendanceResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public AttendanceResponse getAttendanceById(UUID id) {
        Attendance att = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found: " + id));
        return AttendanceResponse.fromEntity(att);
    }

    @Transactional(readOnly = true)
    public MonthlySummaryResponse getMonthlySummary(UUID employeeId, String month) {
        // Resolve Employee Name
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));

        YearMonth ym = YearMonth.parse(month); // Parses YYYY-MM
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Attendance> attendances = attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, start, end);

        long presents = attendances.stream().filter(a -> "PRESENT".equals(a.getStatus())).count();
        long lates = attendances.stream().filter(a -> "LATE".equals(a.getStatus())).count();
        long halfDays = attendances.stream().filter(a -> "HALF_DAY".equals(a.getStatus())).count();
        long absents = attendances.stream().filter(a -> "ABSENT".equals(a.getStatus())).count();

        return MonthlySummaryResponse.builder()
                .employeeId(employeeId)
                .employeeName(emp.getFirstName() + " " + emp.getLastName())
                .month(month)
                .totalPresent(presents)
                .totalLate(lates)
                .totalHalfDay(halfDays)
                .totalAbsent(absents)
                .build();
    }
}
