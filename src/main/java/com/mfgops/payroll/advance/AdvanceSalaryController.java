package com.mfgops.payroll.advance;

import com.mfgops.common.StandardResponse;
import com.mfgops.payroll.advance.dto.AdvanceRequest;
import com.mfgops.payroll.advance.dto.AdvanceResponse;
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
@RequestMapping("/api/advances")
@RequiredArgsConstructor
public class AdvanceSalaryController {

    private final AdvanceSalaryService advanceSalaryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<AdvanceResponse>> requestAdvance(@Valid @RequestBody AdvanceRequest request) {
        AdvanceResponse response = advanceSalaryService.requestAdvance(request);
        return ResponseEntity.ok(StandardResponse.success(response, "Advance salary requested successfully."));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<AdvanceResponse>> approveAdvance(
            @PathVariable UUID id,
            @RequestParam String status) {
        AdvanceResponse response = advanceSalaryService.approveAdvance(id, status);
        return ResponseEntity.ok(StandardResponse.success(response, "Advance salary request processed successfully."));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<Page<AdvanceResponse>>> getAdvancesByEmployeeId(
            @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "requestDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<AdvanceResponse> advances = advanceSalaryService.getAdvancesByEmployeeId(employeeId, pageRequest);
        return ResponseEntity.ok(StandardResponse.success(advances, "Advances list for employee retrieved successfully."));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<Page<AdvanceResponse>>> getAllAdvances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "requestDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<AdvanceResponse> advances = advanceSalaryService.getAllAdvances(pageRequest);
        return ResponseEntity.ok(StandardResponse.success(advances, "All advances list retrieved successfully."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<AdvanceResponse>> getAdvanceById(@PathVariable UUID id) {
        AdvanceResponse response = advanceSalaryService.getAdvanceById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Advance request retrieved successfully."));
    }
}
