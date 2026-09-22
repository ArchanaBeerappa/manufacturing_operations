package com.mfgops.workforce.shift.dto;

import com.mfgops.workforce.shift.Shift;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftResponse {
    private UUID id;
    private UUID tenantId;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ShiftResponse fromEntity(Shift shift) {
        return ShiftResponse.builder()
                .id(shift.getId())
                .tenantId(shift.getTenantId())
                .name(shift.getName())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .createdAt(shift.getCreatedAt())
                .updatedAt(shift.getUpdatedAt())
                .build();
    }
}
