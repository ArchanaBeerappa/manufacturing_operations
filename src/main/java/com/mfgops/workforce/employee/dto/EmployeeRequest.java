package com.mfgops.workforce.employee.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Department is required")
    private String department;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    @NotBlank(message = "Salary type is required")
    @Pattern(regexp = "FIXED_MONTHLY|DAILY_WAGE", message = "Salary type must be either FIXED_MONTHLY or DAILY_WAGE")
    private String salaryType;

    @NotNull(message = "Monthly salary rate is required")
    @DecimalMin(value = "0.00", message = "Monthly salary rate cannot be negative")
    private BigDecimal monthlySalaryRate;

    @NotNull(message = "Daily wage rate is required")
    @DecimalMin(value = "0.00", message = "Daily wage rate cannot be negative")
    private BigDecimal dailyWageRate;
}
