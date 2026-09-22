package com.mfgops.common;

import com.mfgops.tenant.TenantContext;
import com.mfgops.common.exception.UnauthorizedException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class TenantBaseEntity extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @PrePersist
    public void prePersistTenant() {
        UUID currentTenantId = TenantContext.getTenantId();
        if (currentTenantId == null) {
            throw new UnauthorizedException("Tenant context is missing! Cannot persist multi-tenant entity.");
        }
        this.tenantId = currentTenantId;
    }
}
