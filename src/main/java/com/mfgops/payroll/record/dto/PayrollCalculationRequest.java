package com.mfgops.payroll.record.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollCalculationRequest {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotBlank(message = "Payroll month is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "Payroll month must be in YYYY-MM format")
    private String payrollMonth;
}
