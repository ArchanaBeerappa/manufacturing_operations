package com.mfgops.workforce.shift;

import com.mfgops.common.StandardResponse;
import com.mfgops.workforce.shift.dto.ShiftRequest;
import com.mfgops.workforce.shift.dto.ShiftResponse;
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
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<ShiftResponse>> createShift(@Valid @RequestBody ShiftRequest request) {
        ShiftResponse response = shiftService.createShift(request);
        return ResponseEntity.ok(StandardResponse.success(response, "Shift created successfully."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<ShiftResponse>> getShiftById(@PathVariable UUID id) {
        ShiftResponse response = shiftService.getShiftById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Shift retrieved successfully."));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<Page<ShiftResponse>>> getAllShifts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<ShiftResponse> shifts = shiftService.getAllShifts(pageRequest);
        return ResponseEntity.ok(StandardResponse.success(shifts, "Shifts list retrieved successfully."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<ShiftResponse>> updateShift(
            @PathVariable UUID id,
            @Valid @RequestBody ShiftRequest request) {
        ShiftResponse response = shiftService.updateShift(id, request);
        return ResponseEntity.ok(StandardResponse.success(response, "Shift updated successfully."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<Void>> deleteShift(@PathVariable UUID id) {
        shiftService.deleteShift(id);
        return ResponseEntity.ok(StandardResponse.success(null, "Shift deleted successfully."));
    }
}
