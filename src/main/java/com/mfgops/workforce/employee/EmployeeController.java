package com.mfgops.workforce.employee;

import com.mfgops.common.StandardResponse;
import com.mfgops.workforce.employee.dto.EmployeeRequest;
import com.mfgops.workforce.employee.dto.EmployeeResponse;
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
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<EmployeeResponse>> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.ok(StandardResponse.success(response, "Employee created successfully."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<EmployeeResponse>> getEmployeeById(@PathVariable UUID id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Employee retrieved successfully."));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<Page<EmployeeResponse>>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<EmployeeResponse> employees = employeeService.getAllEmployees(pageRequest);
        return ResponseEntity.ok(StandardResponse.success(employees, "Employees list retrieved successfully."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<EmployeeResponse>> updateEmployee(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(StandardResponse.success(response, "Employee updated successfully."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<Void>> deleteEmployee(@PathVariable UUID id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.ok(StandardResponse.success(null, "Employee deleted successfully."));
    }
}
