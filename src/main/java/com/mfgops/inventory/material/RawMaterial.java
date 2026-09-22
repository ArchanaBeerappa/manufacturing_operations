package com.mfgops.inventory.material;

import com.mfgops.common.TenantBaseEntity;
import com.mfgops.inventory.supplier.Supplier;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "raw_materials", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "sku"})
})
@SQLRestriction("deleted_at IS NULL")
public class RawMaterial extends TenantBaseEntity {

    @Column(name = "sku", nullable = false)
    private String sku;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "unit", nullable = false)
    private String unit;

    @Column(name = "quantity", nullable = false, precision = 15, scale = 4)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "reorder_level", nullable = false, precision = 15, scale = 4)
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
