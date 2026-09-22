package com.mfgops.payroll.record.dto;

import com.mfgops.payroll.record.PayrollRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollResponse {
    private UUID id;
    private UUID tenantId;
    private UUID employeeId;
    private String employeeName;
    private String employeeRole;
    private String salaryType;
    private String payrollMonth;
    private BigDecimal baseSalary;
    private BigDecimal dailyWageEarnings;
    private BigDecimal overtimeEarnings;
    private BigDecimal advanceDeductions;
    private BigDecimal otherDeductions;
    private BigDecimal netSalary;
    private String status;
    private LocalDateTime paidAt;

    public static PayrollResponse fromEntity(PayrollRecord record) {
        return PayrollResponse.builder()
                .id(record.getId())
                .tenantId(record.getTenantId())
                .employeeId(record.getEmployee().getId())
                .employeeName(record.getEmployee().getFirstName() + " " + record.getEmployee().getLastName())
                .employeeRole(record.getEmployee().getRole())
                .salaryType(record.getEmployee().getSalaryType())
                .payrollMonth(record.getPayrollMonth())
                .baseSalary(record.getBaseSalary())
                .dailyWageEarnings(record.getDailyWageEarnings())
                .overtimeEarnings(record.getOvertimeEarnings())
                .advanceDeductions(record.getAdvanceDeductions())
                .otherDeductions(record.getOtherDeductions())
                .netSalary(record.getNetSalary())
                .status(record.getStatus())
                .paidAt(record.getPaidAt())
                .build();
    }
}
