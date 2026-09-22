package com.mfgops.workforce.attendance.dto;

import com.mfgops.workforce.attendance.Attendance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {
    private UUID id;
    private UUID tenantId;
    private UUID employeeId;
    private String employeeName;
    private LocalDate date;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private UUID shiftId;
    private String shiftName;
    private String status;
    private UUID correctedById;
    private String correctedByName;
    private String correctionReason;
    private LocalDateTime correctedAt;

    public static AttendanceResponse fromEntity(Attendance att) {
        UUID correctedById = att.getCorrectedBy() != null ? att.getCorrectedBy().getId() : null;
        String correctedByName = att.getCorrectedBy() != null ? att.getCorrectedBy().getUsername() : null;

        return AttendanceResponse.builder()
                .id(att.getId())
                .tenantId(att.getTenantId())
                .employeeId(att.getEmployee().getId())
                .employeeName(att.getEmployee().getFirstName() + " " + att.getEmployee().getLastName())
                .date(att.getDate())
                .checkInTime(att.getCheckInTime())
                .checkOutTime(att.getCheckOutTime())
                .shiftId(att.getShift() != null ? att.getShift().getId() : null)
                .shiftName(att.getShift() != null ? att.getShift().getName() : null)
                .status(att.getStatus())
                .correctedById(correctedById)
                .correctedByName(correctedByName)
                .correctionReason(att.getCorrectionReason())
                .correctedAt(att.getCorrectedAt())
                .build();
    }
}
