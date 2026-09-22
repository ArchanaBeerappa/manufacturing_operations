package com.mfgops.dashboard;

import com.mfgops.workforce.employee.Employee;
import com.mfgops.workforce.employee.EmployeeRepository;
import com.mfgops.inventory.material.RawMaterial;
import com.mfgops.inventory.material.RawMaterialRepository;
import com.mfgops.payroll.record.PayrollRecord;
import com.mfgops.payroll.record.PayrollRecordRepository;
import com.mfgops.expense.Expense;
import com.mfgops.expense.ExpenseRepository;
import com.mfgops.dashboard.dto.DashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final PayrollRecordRepository payrollRecordRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardStats(String month) {
        // 1. Workforce Headcount
        long activeHeadcount = employeeRepository.findAll().stream()
                .filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus()))
                .count();

        // 2. Inventory Alarms (PageRequest of size 1 gets the total elements cleanly from metadata)
        long lowStockCount = rawMaterialRepository.findLowStock(PageRequest.of(0, 1)).getTotalElements();

        // 3. Monthly Payroll Costs
        BigDecimal payrollCost = payrollRecordRepository.findAll().stream()
                .filter(pr -> month.equalsIgnoreCase(pr.getPayrollMonth()))
                .map(PayrollRecord::getNetSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Monthly Operational Expenses Burn
        YearMonth ym = YearMonth.parse(month); // YYYY-MM
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59, 999999999);

        BigDecimal expenseBurn = expenseRepository.findBySubmittedAtBetween(start, end).stream()
                .filter(e -> !"REJECTED".equalsIgnoreCase(e.getStatus()))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardResponse.builder()
                .month(month)
                .activeHeadcount(activeHeadcount)
                .monthlyPayrollCost(payrollCost)
                .lowStockAlertsCount(lowStockCount)
                .monthlyExpenseBurn(expenseBurn)
                .currency("INR")
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] generateConsolidatedCsvReport(String month) {
        DashboardResponse stats = getDashboardStats(month);
        
        YearMonth ym = YearMonth.parse(month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59, 999999999);

        List<RawMaterial> materials = rawMaterialRepository.findAll();
        List<Expense> expenses = expenseRepository.findBySubmittedAtBetween(start, end);

        StringBuilder csv = new StringBuilder();

        // Section 1: Factory Highlights Summary
        csv.append("FABRICORE MANUFACTURING PLATFORMS - CONSOLIDATED OPERATIONAL SUMMARY\n");
        csv.append("Report Calendar Month: ").append(month).append("\n");
        csv.append("-----------------------------------------------------------------------\n\n");
        csv.append("METRIC NAME,METRIC VALUE,CURRENCY/UNIT\n");
        csv.append("Factory Active Headcount,").append(stats.getActiveHeadcount()).append(",Employees\n");
        csv.append("Total Payroll Net Disbursements,").append(stats.getMonthlyPayrollCost()).append(",INR\n");
        csv.append("Active Low Stock Inventory Alarms,").append(stats.getLowStockAlertsCount()).append(",Materials\n");
        csv.append("Total Approved Expense Burn,").append(stats.getMonthlyExpenseBurn()).append(",INR\n");
        csv.append("-----------------------------------------------------------------------\n\n\n");

        // Section 2: Detailed Inventory Status Details
        csv.append("SECTION B: DETAILED MATERIAL STOCK REPORT\n");
        csv.append("SKU,Material Name,Unit Of Measure,Current Quantity,Reorder Threshold,Alarm Status\n");
        for (RawMaterial rm : materials) {
            boolean isLow = rm.getQuantity().compareTo(rm.getReorderLevel()) <= 0;
            csv.append(rm.getSku()).append(",")
               .append(rm.getName()).append(",")
               .append(rm.getUnit()).append(",")
               .append(rm.getQuantity()).append(",")
               .append(rm.getReorderLevel()).append(",")
               .append(isLow ? "LOW STOCK ALERT" : "OK").append("\n");
        }
        csv.append("\n\n");

        // Section 3: Itemized Monthly Expenses Details
        csv.append("SECTION C: DETAILED MONTHLY OPERATIONAL EXPENSES LOGS\n");
        csv.append("Date Submitted,Expense Description,Category,Department,Project,Amount (INR),Workflow Status\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Expense exp : expenses) {
            String dept = exp.getDepartment() != null ? exp.getDepartment() : "N/A";
            String proj = exp.getProject() != null ? exp.getProject() : "N/A";
            csv.append(exp.getSubmittedAt().format(formatter)).append(",")
               .append(exp.getDescription().replace(",", " ")).append(",")
               .append(exp.getCategory()).append(",")
               .append(dept).append(",")
               .append(proj).append(",")
               .append(exp.getAmount()).append(",")
               .append(exp.getStatus()).append("\n");
        }

        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
