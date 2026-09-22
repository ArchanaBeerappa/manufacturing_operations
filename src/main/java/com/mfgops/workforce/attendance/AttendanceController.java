package com.mfgops.workforce.attendance;

import com.mfgops.common.StandardResponse;
import com.mfgops.workforce.attendance.dto.AttendanceRequest;
import com.mfgops.workforce.attendance.dto.AttendanceResponse;
import com.mfgops.workforce.attendance.dto.AttendanceCorrectionRequest;
import com.mfgops.workforce.attendance.dto.MonthlySummaryResponse;
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
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<AttendanceResponse>> checkIn(@Valid @RequestBody AttendanceRequest request) {
        AttendanceResponse response = attendanceService.checkIn(request);
        return ResponseEntity.ok(StandardResponse.success(response, "Check-in recorded successfully."));
    }

    @PostMapping("/check-out/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<AttendanceResponse>> checkOut(@PathVariable UUID employeeId) {
        AttendanceResponse response = attendanceService.checkOut(employeeId);
        return ResponseEntity.ok(StandardResponse.success(response, "Check-out recorded successfully."));
    }

    @PutMapping("/correct/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<AttendanceResponse>> correctAttendance(
            @PathVariable UUID id,
            @Valid @RequestBody AttendanceCorrectionRequest request) {
        AttendanceResponse response = attendanceService.correctAttendance(id, request);
        return ResponseEntity.ok(StandardResponse.success(response, "Attendance record corrected successfully."));
    }

    @GetMapping("/summary/{employeeId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<MonthlySummaryResponse>> getMonthlySummary(
            @PathVariable UUID employeeId,
            @RequestParam String month) {
        // month expects YYYY-MM
        MonthlySummaryResponse response = attendanceService.getMonthlySummary(employeeId, month);
        return ResponseEntity.ok(StandardResponse.success(response, "Monthly attendance summary retrieved successfully."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<AttendanceResponse>> getAttendanceById(@PathVariable UUID id) {
        AttendanceResponse response = attendanceService.getAttendanceById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Attendance record retrieved successfully."));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<Page<AttendanceResponse>>> getAllAttendances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<AttendanceResponse> attendances = attendanceService.getAllAttendances(pageRequest);
        return ResponseEntity.ok(StandardResponse.success(attendances, "Attendance list retrieved successfully."));
    }
}
