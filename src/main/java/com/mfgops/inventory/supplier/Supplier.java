package com.mfgops.inventory.supplier;

import com.mfgops.common.TenantBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "suppliers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "name"})
})
public class Supplier extends TenantBaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;
}
