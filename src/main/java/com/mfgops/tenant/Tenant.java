package com.mfgops.tenant;

import com.mfgops.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tenants")
public class Tenant extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "subdomain", unique = true)
    private String subdomain;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";
}
