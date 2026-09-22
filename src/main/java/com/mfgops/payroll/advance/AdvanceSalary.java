package com.mfgops.payroll.advance;

import com.mfgops.auth.User;
import com.mfgops.common.TenantBaseEntity;
import com.mfgops.workforce.employee.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "advance_salaries")
public class AdvanceSalary extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "status", nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, DEDUCTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "deduction_month", nullable = false)
    private String deductionMonth; // YYYY-MM
}
