package com.mfgops.inventory.supplier;

import com.mfgops.common.StandardResponse;
import com.mfgops.inventory.supplier.dto.SupplierRequest;
import com.mfgops.inventory.supplier.dto.SupplierResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<SupplierResponse>> createSupplier(@Valid @RequestBody SupplierRequest request) {
        SupplierResponse response = supplierService.createSupplier(request);
        return ResponseEntity.ok(StandardResponse.success(response, "Supplier created successfully."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<SupplierResponse>> getSupplierById(@PathVariable UUID id) {
        SupplierResponse response = supplierService.getSupplierById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Supplier retrieved successfully."));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<Page<SupplierResponse>>> getAllSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<SupplierResponse> suppliers = supplierService.getAllSuppliers(pageRequest);
        return ResponseEntity.ok(StandardResponse.success(suppliers, "Suppliers list retrieved successfully."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<SupplierResponse>> updateSupplier(
            @PathVariable UUID id,
            @Valid @RequestBody SupplierRequest request) {
        SupplierResponse response = supplierService.updateSupplier(id, request);
        return ResponseEntity.ok(StandardResponse.success(response, "Supplier updated successfully."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<Void>> deleteSupplier(@PathVariable UUID id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok(StandardResponse.success(null, "Supplier deleted successfully."));
    }
}
