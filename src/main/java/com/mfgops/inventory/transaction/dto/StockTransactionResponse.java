package com.mfgops.inventory.transaction.dto;

import com.mfgops.inventory.transaction.StockTransaction;
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
public class StockTransactionResponse {
    private UUID id;
    private UUID tenantId;
    private UUID materialId;
    private String materialSku;
    private String materialName;
    private String transactionType;
    private BigDecimal quantity;
    private String remarks;
    private LocalDateTime timestamp;
    private UUID userId;
    private String username;

    public static StockTransactionResponse fromEntity(StockTransaction tx) {
        UUID uId = tx.getUser() != null ? tx.getUser().getId() : null;
        String uName = tx.getUser() != null ? tx.getUser().getUsername() : null;

        return StockTransactionResponse.builder()
                .id(tx.getId())
                .tenantId(tx.getTenantId())
                .materialId(tx.getMaterial().getId())
                .materialSku(tx.getMaterial().getSku())
                .materialName(tx.getMaterial().getName())
                .transactionType(tx.getTransactionType())
                .quantity(tx.getQuantity())
                .remarks(tx.getRemarks())
                .timestamp(tx.getTimestamp())
                .userId(uId)
                .username(uName)
                .build();
    }
}
