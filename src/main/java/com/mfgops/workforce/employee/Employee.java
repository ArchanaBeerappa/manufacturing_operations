package com.mfgops.workforce.employee;

import com.mfgops.common.TenantBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "employees")
@SQLRestriction("deleted_at IS NULL")
public class Employee extends TenantBaseEntity {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "role", nullable = false)
    private String role; // OPERATOR, SUPERVISOR, HELPER, etc.

    @Column(name = "department", nullable = false)
    private String department; // PRODUCTION, ASSEMBLY, LOGISTICS, etc.

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "salary_type", nullable = false)
    private String salaryType; // FIXED_MONTHLY, DAILY_WAGE

    @Column(name = "monthly_salary_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlySalaryRate = BigDecimal.ZERO;

    @Column(name = "daily_wage_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal dailyWageRate = BigDecimal.ZERO;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE"; // ACTIVE, TERMINATED, ON_LEAVE

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
