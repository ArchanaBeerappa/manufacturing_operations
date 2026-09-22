package com.mfgops.tenant;

import com.mfgops.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public Tenant getTenantById(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + tenantId));
    }

    @Transactional(readOnly = true)
    public Tenant getTenantBySubdomain(String subdomain) {
        return tenantRepository.findBySubdomain(subdomain)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with subdomain: " + subdomain));
    }
}
