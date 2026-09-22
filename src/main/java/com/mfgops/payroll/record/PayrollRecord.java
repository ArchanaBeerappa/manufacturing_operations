package com.mfgops.payroll.record;

import com.mfgops.common.TenantBaseEntity;
import com.mfgops.workforce.employee.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "payroll_records", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "employee_id", "payroll_month"})
})
public class PayrollRecord extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "payroll_month", nullable = false)
    private String payrollMonth; // YYYY-MM

    @Column(name = "base_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseSalary = BigDecimal.ZERO;

    @Column(name = "daily_wage_earnings", nullable = false, precision = 12, scale = 2)
    private BigDecimal dailyWageEarnings = BigDecimal.ZERO;

    @Column(name = "overtime_earnings", nullable = false, precision = 12, scale = 2)
    private BigDecimal overtimeEarnings = BigDecimal.ZERO;

    @Column(name = "advance_deductions", nullable = false, precision = 12, scale = 2)
    private BigDecimal advanceDeductions = BigDecimal.ZERO;

    @Column(name = "other_deductions", nullable = false, precision = 12, scale = 2)
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    @Column(name = "net_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal netSalary = BigDecimal.ZERO;

    @Column(name = "status", nullable = false)
    private String status = "DRAFT"; // DRAFT, APPROVED, PAID

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
