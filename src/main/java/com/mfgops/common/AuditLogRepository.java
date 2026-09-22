package com.mfgops.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Page<AuditLog> findByTenantId(UUID tenantId, Pageable pageable);
    Page<AuditLog> findByTenantIdAndEntityTypeAndEntityId(UUID tenantId, String entityType, UUID entityId, Pageable pageable);
}
