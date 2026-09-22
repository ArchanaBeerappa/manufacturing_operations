package com.mfgops.workforce.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequest {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotNull(message = "Shift ID is required")
    private UUID shiftId;
}
