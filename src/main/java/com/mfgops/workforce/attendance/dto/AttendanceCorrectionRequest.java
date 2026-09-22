package com.mfgops.workforce.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCorrectionRequest {

    @NotNull(message = "Corrected check-in time is required")
    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    @NotBlank(message = "Correction reason is required")
    private String reason;

    @NotBlank(message = "Status is required (e.g. PRESENT, LATE, HALF_DAY, ABSENT)")
    private String status;
}
