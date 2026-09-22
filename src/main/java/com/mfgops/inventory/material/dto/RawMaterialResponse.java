package com.mfgops.inventory.material.dto;

import com.mfgops.inventory.material.RawMaterial;
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
public class RawMaterialResponse {
    private UUID id;
    private UUID tenantId;
    private String sku;
    private String name;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal reorderLevel;
    private UUID supplierId;
    private String supplierName;
    private boolean lowStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RawMaterialResponse fromEntity(RawMaterial material) {
        boolean isLowStock = material.getQuantity().compareTo(material.getReorderLevel()) <= 0;
        UUID suppId = material.getSupplier() != null ? material.getSupplier().getId() : null;
        String suppName = material.getSupplier() != null ? material.getSupplier().getName() : null;

        return RawMaterialResponse.builder()
                .id(material.getId())
                .tenantId(material.getTenantId())
                .sku(material.getSku())
                .name(material.getName())
                .unit(material.getUnit())
                .quantity(material.getQuantity())
                .reorderLevel(material.getReorderLevel())
                .supplierId(suppId)
                .supplierName(suppName)
                .lowStock(isLowStock)
                .createdAt(material.getCreatedAt())
                .updatedAt(material.getUpdatedAt())
                .build();
    }
}
