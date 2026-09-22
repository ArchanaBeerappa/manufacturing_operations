package com.mfgops.payroll.record;

import com.mfgops.common.StandardResponse;
import com.mfgops.payroll.record.dto.PayrollCalculationRequest;
import com.mfgops.payroll.record.dto.PayrollResponse;
import com.mfgops.payroll.pdf.PayslipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;
    private final PayslipService payslipService;

    @PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<PayrollResponse>> calculatePayroll(
            @Valid @RequestBody PayrollCalculationRequest request) {
        PayrollResponse response = payrollService.calculatePayroll(request);
        return ResponseEntity.ok(StandardResponse.success(response, "Payroll calculated successfully."));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<PayrollResponse>> approvePayroll(@PathVariable UUID id) {
        PayrollResponse response = payrollService.approvePayroll(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Payroll approved successfully."));
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<PayrollResponse>> paySalary(@PathVariable UUID id) {
        PayrollResponse response = payrollService.paySalary(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Salary paid successfully."));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<Page<PayrollResponse>>> getPayrollByEmployeeId(
            @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "payrollMonth") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<PayrollResponse> history = payrollService.getPayrollByEmployeeId(employeeId, pageRequest);
        return ResponseEntity.ok(StandardResponse.success(history, "Payroll history retrieved successfully."));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<Page<PayrollResponse>>> getAllPayrollRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "payrollMonth") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<PayrollResponse> records = payrollService.getAllPayrollRecords(pageRequest);
        return ResponseEntity.ok(StandardResponse.success(records, "All payroll records retrieved successfully."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<PayrollResponse>> getPayrollById(@PathVariable UUID id) {
        PayrollRecord entity = payrollService.getPayrollRecordEntityById(id);
        return ResponseEntity.ok(StandardResponse.success(PayrollResponse.fromEntity(entity), "Payroll record retrieved successfully."));
    }

    @GetMapping("/{id}/payslip")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<byte[]> downloadPayslip(@PathVariable UUID id) {
        PayrollRecord record = payrollService.getPayrollRecordEntityById(id);
        byte[] pdfBytes = payslipService.generatePayslipPdf(record);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "payslip_" + record.getPayrollMonth() + "_" + record.getId() + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
