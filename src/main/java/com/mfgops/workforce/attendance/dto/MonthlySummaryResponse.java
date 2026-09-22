package com.mfgops.workforce.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySummaryResponse {
    private UUID employeeId;
    private String employeeName;
    private String month; // YYYY-MM
    private long totalPresent;
    private long totalLate;
    private long totalHalfDay;
    private long totalAbsent;
}
