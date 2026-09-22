package com.mfgops.workforce.shift;

import com.mfgops.common.AuditLogService;
import com.mfgops.common.exception.ResourceNotFoundException;
import com.mfgops.workforce.shift.dto.ShiftRequest;
import com.mfgops.workforce.shift.dto.ShiftResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public ShiftResponse createShift(ShiftRequest request) {
        if (shiftRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Shift with name '" + request.getName() + "' already exists.");
        }

        Shift shift = new Shift();
        shift.setName(request.getName().toUpperCase());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());

        Shift saved = shiftRepository.save(shift);

        // Explicit Auditing
        auditLogService.log(saved.getTenantId(), "Shift", saved.getId(), "CREATE", null, saved);

        return ShiftResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ShiftResponse getShiftById(UUID id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + id));
        return ShiftResponse.fromEntity(shift);
    }

    @Transactional(readOnly = true)
    public Page<ShiftResponse> getAllShifts(Pageable pageable) {
        return shiftRepository.findAll(pageable)
                .map(ShiftResponse::fromEntity);
    }

    @Transactional
    public ShiftResponse updateShift(UUID id, ShiftRequest request) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + id));

        // Enforce unique name on update if changed
        if (!shift.getName().equalsIgnoreCase(request.getName()) && shiftRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Shift with name '" + request.getName() + "' already exists.");
        }

        // Snapshot old state for auditing
        Shift oldState = new Shift();
        oldState.setId(shift.getId());
        oldState.setTenantId(shift.getTenantId());
        oldState.setName(shift.getName());
        oldState.setStartTime(shift.getStartTime());
        oldState.setEndTime(shift.getEndTime());
        oldState.setCreatedAt(shift.getCreatedAt());

        // Apply changes
        shift.setName(request.getName().toUpperCase());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());

        Shift updated = shiftRepository.save(shift);

        // Explicit Auditing
        auditLogService.log(updated.getTenantId(), "Shift", updated.getId(), "UPDATE", oldState, updated);

        return ShiftResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteShift(UUID id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + id));

        // Snapshot old state for auditing
        Shift oldState = new Shift();
        oldState.setId(shift.getId());
        oldState.setTenantId(shift.getTenantId());
        oldState.setName(shift.getName());

        shiftRepository.delete(shift);

        // Explicit Auditing
        auditLogService.log(shift.getTenantId(), "Shift", id, "DELETE", oldState, null);
    }
}
