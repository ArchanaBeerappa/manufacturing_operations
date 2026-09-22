package com.mfgops.inventory.supplier;

import com.mfgops.common.AuditLogService;
import com.mfgops.common.exception.ResourceNotFoundException;
import com.mfgops.inventory.supplier.dto.SupplierRequest;
import com.mfgops.inventory.supplier.dto.SupplierResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        if (supplierRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Supplier with name '" + request.getName() + "' already exists.");
        }

        Supplier supplier = new Supplier();
        supplier.setName(request.getName());
        supplier.setContactName(request.getContactName());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());

        Supplier saved = supplierRepository.save(supplier);

        // Explicit Auditing
        auditLogService.log(saved.getTenantId(), "Supplier", saved.getId(), "CREATE", null, saved);

        return SupplierResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));
        return SupplierResponse.fromEntity(supplier);
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> getAllSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable)
                .map(SupplierResponse::fromEntity);
    }

    @Transactional
    public SupplierResponse updateSupplier(UUID id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));

        // Enforce unique name on update if changed
        if (!supplier.getName().equalsIgnoreCase(request.getName()) && supplierRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Supplier with name '" + request.getName() + "' already exists.");
        }

        // Snapshot old state for auditing
        Supplier oldState = new Supplier();
        oldState.setId(supplier.getId());
        oldState.setTenantId(supplier.getTenantId());
        oldState.setName(supplier.getName());
        oldState.setContactName(supplier.getContactName());
        oldState.setEmail(supplier.getEmail());
        oldState.setPhone(supplier.getPhone());
        oldState.setAddress(supplier.getAddress());
        oldState.setCreatedAt(supplier.getCreatedAt());

        // Apply changes
        supplier.setName(request.getName());
        supplier.setContactName(request.getContactName());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());

        Supplier updated = supplierRepository.save(supplier);

        // Explicit Auditing
        auditLogService.log(updated.getTenantId(), "Supplier", updated.getId(), "UPDATE", oldState, updated);

        return SupplierResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteSupplier(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));

        // Snapshot old state for auditing
        Supplier oldState = new Supplier();
        oldState.setId(supplier.getId());
        oldState.setTenantId(supplier.getTenantId());
        oldState.setName(supplier.getName());
        oldState.setContactName(supplier.getContactName());
        oldState.setEmail(supplier.getEmail());
        oldState.setPhone(supplier.getPhone());
        oldState.setAddress(supplier.getAddress());

        supplierRepository.delete(supplier);

        // Explicit Auditing
        auditLogService.log(supplier.getTenantId(), "Supplier", id, "DELETE", oldState, null);
    }
}
