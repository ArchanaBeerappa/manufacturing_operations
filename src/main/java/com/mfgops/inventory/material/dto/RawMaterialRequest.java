package com.mfgops.inventory.material.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterialRequest {

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotBlank(message = "Material name is required")
    private String name;

    @NotBlank(message = "Unit of measure is required")
    private String unit;

    @NotNull(message = "Reorder level is required")
    @DecimalMin(value = "0.0000", message = "Reorder level cannot be negative")
    private BigDecimal reorderLevel;

    private UUID supplierId;
}
