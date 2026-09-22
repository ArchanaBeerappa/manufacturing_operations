package com.mfgops.inventory.transaction;

import com.mfgops.auth.User;
import com.mfgops.common.TenantBaseEntity;
import com.mfgops.inventory.material.RawMaterial;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "stock_transactions")
public class StockTransaction extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private RawMaterial material;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType; // STOCK_IN, STOCK_OUT

    @Column(name = "quantity", nullable = false, precision = 15, scale = 4)
    private BigDecimal quantity;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
