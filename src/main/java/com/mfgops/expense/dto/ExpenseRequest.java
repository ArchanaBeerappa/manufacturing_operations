package com.mfgops.expense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseRequest {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Expense amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Category is required")
    @Pattern(regexp = "UTILITIES|MAINTENANCE|TRANSPORT|MISC", message = "Category must be UTILITIES, MAINTENANCE, TRANSPORT, or MISC")
    private String category;

    private String department;

    private String project;
}
