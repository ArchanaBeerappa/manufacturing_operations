package com.mfgops.inventory.transaction.dto;

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
public class StockTransactionRequest {

    @NotNull(message = "Material ID is required")
    private UUID materialId;

    @NotBlank(message = "Transaction type is required")
    @Pattern(regexp = "STOCK_IN|STOCK_OUT", message = "Transaction type must be either STOCK_IN or STOCK_OUT")
    private String transactionType;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    private String remarks;
}
