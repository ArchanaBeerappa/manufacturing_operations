package com.mfgops.inventory.material;

import com.mfgops.common.StandardResponse;
import com.mfgops.inventory.material.dto.RawMaterialRequest;
import com.mfgops.inventory.material.dto.RawMaterialResponse;
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
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class RawMaterialController {

    private final RawMaterialService rawMaterialService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<RawMaterialResponse>> createMaterial(@Valid @RequestBody RawMaterialRequest request) {
        RawMaterialResponse response = rawMaterialService.createMaterial(request);
        return ResponseEntity.ok(StandardResponse.success(response, "Raw material created successfully."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<RawMaterialResponse>> getMaterialById(@PathVariable UUID id) {
        RawMaterialResponse response = rawMaterialService.getMaterialById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Raw material retrieved successfully."));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<Page<RawMaterialResponse>>> getAllMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sku") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<RawMaterialResponse> materials = rawMaterialService.getAllMaterials(pageRequest);
        return ResponseEntity.ok(StandardResponse.success(materials, "Raw materials list retrieved successfully."));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<Page<RawMaterialResponse>>> getLowStockMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sku") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<RawMaterialResponse> materials = rawMaterialService.getLowStockMaterials(pageRequest);
        return ResponseEntity.ok(StandardResponse.success(materials, "Low stock raw materials retrieved successfully."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<RawMaterialResponse>> updateMaterial(
            @PathVariable UUID id,
            @Valid @RequestBody RawMaterialRequest request) {
        RawMaterialResponse response = rawMaterialService.updateMaterial(id, request);
        return ResponseEntity.ok(StandardResponse.success(response, "Raw material updated successfully."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<Void>> deleteMaterial(@PathVariable UUID id) {
        rawMaterialService.softDeleteMaterial(id);
        return ResponseEntity.ok(StandardResponse.success(null, "Raw material deleted successfully."));
    }
}
