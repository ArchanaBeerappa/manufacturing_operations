package com.mfgops.inventory.material;

import com.mfgops.common.AuditLogService;
import com.mfgops.common.exception.ResourceNotFoundException;
import com.mfgops.inventory.material.dto.RawMaterialRequest;
import com.mfgops.inventory.material.dto.RawMaterialResponse;
import com.mfgops.inventory.supplier.Supplier;
import com.mfgops.inventory.supplier.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RawMaterialService {

    private final RawMaterialRepository rawMaterialRepository;
    private final SupplierRepository supplierRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public RawMaterialResponse createMaterial(RawMaterialRequest request) {
        if (rawMaterialRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("Raw material with SKU '" + request.getSku() + "' already exists.");
        }

        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.getSupplierId()));
        }

        RawMaterial material = new RawMaterial();
        material.setSku(request.getSku());
        material.setName(request.getName());
        material.setUnit(request.getUnit());
        material.setQuantity(BigDecimal.ZERO); // Quantity strictly initialized to 0
        material.setReorderLevel(request.getReorderLevel());
        material.setSupplier(supplier);

        RawMaterial saved = rawMaterialRepository.save(material);

        // Explicit Auditing
        auditLogService.log(saved.getTenantId(), "RawMaterial", saved.getId(), "CREATE", null, saved);

        return RawMaterialResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public RawMaterialResponse getMaterialById(UUID id) {
        RawMaterial material = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Raw material not found: " + id));
        return RawMaterialResponse.fromEntity(material);
    }

    @Transactional(readOnly = true)
    public Page<RawMaterialResponse> getAllMaterials(Pageable pageable) {
        return rawMaterialRepository.findAll(pageable)
                .map(RawMaterialResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<RawMaterialResponse> getLowStockMaterials(Pageable pageable) {
        return rawMaterialRepository.findLowStock(pageable)
                .map(RawMaterialResponse::fromEntity);
    }

    @Transactional
    public RawMaterialResponse updateMaterial(UUID id, RawMaterialRequest request) {
        RawMaterial material = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Raw material not found: " + id));

        // Enforce unique SKU
        if (!material.getSku().equalsIgnoreCase(request.getSku()) && rawMaterialRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("Raw material with SKU '" + request.getSku() + "' already exists.");
        }

        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.getSupplierId()));
        }

        // Snapshot old state for auditing (Note: quantity is not modifiable via update API)
        RawMaterial oldState = new RawMaterial();
        oldState.setId(material.getId());
        oldState.setTenantId(material.getTenantId());
        oldState.setSku(material.getSku());
        oldState.setName(material.getName());
        oldState.setUnit(material.getUnit());
        oldState.setQuantity(material.getQuantity());
        oldState.setReorderLevel(material.getReorderLevel());
        oldState.setSupplier(material.getSupplier());
        oldState.setCreatedAt(material.getCreatedAt());

        // Apply changes
        material.setSku(request.getSku());
        material.setName(request.getName());
        material.setUnit(request.getUnit());
        material.setReorderLevel(request.getReorderLevel());
        material.setSupplier(supplier);

        RawMaterial updated = rawMaterialRepository.save(material);

        // Explicit Auditing
        auditLogService.log(updated.getTenantId(), "RawMaterial", updated.getId(), "UPDATE", oldState, updated);

        return RawMaterialResponse.fromEntity(updated);
    }

    @Transactional
    public void softDeleteMaterial(UUID id) {
        RawMaterial material = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Raw material not found: " + id));

        // Snapshot old state
        RawMaterial oldState = new RawMaterial();
        oldState.setId(material.getId());
        oldState.setTenantId(material.getTenantId());
        oldState.setSku(material.getSku());
        oldState.setName(material.getName());
        oldState.setQuantity(material.getQuantity());

        // Perform Soft Delete
        material.setDeletedAt(LocalDateTime.now());
        rawMaterialRepository.save(material);

        // Explicit Auditing
        auditLogService.log(material.getTenantId(), "RawMaterial", id, "DELETE", oldState, null);
    }
}
