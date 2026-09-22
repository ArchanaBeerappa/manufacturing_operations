package com.mfgops.payroll.advance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdvanceRequest {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotNull(message = "Advance amount is required")
    @DecimalMin(value = "0.01", message = "Advance amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Deduction month is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "Deduction month must be in YYYY-MM format")
    private String deductionMonth;
}
