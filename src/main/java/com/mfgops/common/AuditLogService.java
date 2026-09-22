package com.mfgops.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void log(UUID tenantId, String entityType, UUID entityId, String action, Object oldValue, Object newValue) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setTenantId(tenantId);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setAction(action);

            if (oldValue != null) {
                auditLog.setOldValue(objectMapper.writeValueAsString(oldValue));
            }
            if (newValue != null) {
                auditLog.setNewValue(objectMapper.writeValueAsString(newValue));
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String changedBy = "SYSTEM";
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                changedBy = authentication.getName();
            }
            auditLog.setChangedBy(changedBy);
            auditLog.setChangedAt(LocalDateTime.now());

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Log warning but do not break parent business flows on serialization glitches
            System.err.println("Failed to serialize audit log: " + e.getMessage());
        }
    }
}
