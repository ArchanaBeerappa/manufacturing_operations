package com.mfgops.inventory.supplier.dto;

import com.mfgops.inventory.supplier.Supplier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String contactName;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SupplierResponse fromEntity(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .tenantId(supplier.getTenantId())
                .name(supplier.getName())
                .contactName(supplier.getContactName())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }
}
