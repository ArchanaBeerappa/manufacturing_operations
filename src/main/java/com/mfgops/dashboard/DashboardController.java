package com.mfgops.dashboard;

import com.mfgops.common.StandardResponse;
import com.mfgops.dashboard.dto.DashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<DashboardResponse>> getDashboardStats(@RequestParam String month) {
        // month expects YYYY-MM
        DashboardResponse stats = dashboardService.getDashboardStats(month);
        return ResponseEntity.ok(StandardResponse.success(stats, "Dashboard statistics compiled successfully."));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<byte[]> exportMonthlyReport(@RequestParam String month) {
        byte[] csvBytes = dashboardService.generateConsolidatedCsvReport(month);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "consolidated_operational_report_" + month + ".csv");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }
}
