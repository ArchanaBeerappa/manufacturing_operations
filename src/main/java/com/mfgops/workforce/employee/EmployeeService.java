package com.mfgops.workforce.employee;

import com.mfgops.common.AuditLogService;
import com.mfgops.common.exception.ResourceNotFoundException;
import com.mfgops.workforce.employee.dto.EmployeeRequest;
import com.mfgops.workforce.employee.dto.EmployeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (request.getEmail() != null && !request.getEmail().isBlank() && employeeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Employee with email '" + request.getEmail() + "' already exists.");
        }

        Employee employee = new Employee();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setRole(request.getRole());
        employee.setDepartment(request.getDepartment());
        employee.setJoiningDate(request.getJoiningDate());
        employee.setSalaryType(request.getSalaryType().toUpperCase());
        employee.setMonthlySalaryRate(request.getMonthlySalaryRate());
        employee.setDailyWageRate(request.getDailyWageRate());
        employee.setStatus("ACTIVE");

        Employee saved = employeeRepository.save(employee);

        // Explicit Auditing
        auditLogService.log(saved.getTenantId(), "Employee", saved.getId(), "CREATE", null, saved);

        return EmployeeResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        return EmployeeResponse.fromEntity(employee);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable)
                .map(EmployeeResponse::fromEntity);
    }

    @Transactional
    public EmployeeResponse updateEmployee(UUID id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));

        // Enforce unique email on update if changed
        if (request.getEmail() != null && !request.getEmail().isBlank() && !request.getEmail().equalsIgnoreCase(employee.getEmail())) {
            if (employeeRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Employee with email '" + request.getEmail() + "' already exists.");
            }
        }

        // Snapshot old state for auditing
        Employee oldState = new Employee();
        oldState.setId(employee.getId());
        oldState.setTenantId(employee.getTenantId());
        oldState.setFirstName(employee.getFirstName());
        oldState.setLastName(employee.getLastName());
        oldState.setEmail(employee.getEmail());
        oldState.setPhone(employee.getPhone());
        oldState.setRole(employee.getRole());
        oldState.setDepartment(employee.getDepartment());
        oldState.setJoiningDate(employee.getJoiningDate());
        oldState.setSalaryType(employee.getSalaryType());
        oldState.setMonthlySalaryRate(employee.getMonthlySalaryRate());
        oldState.setDailyWageRate(employee.getDailyWageRate());
        oldState.setStatus(employee.getStatus());
        oldState.setCreatedAt(employee.getCreatedAt());

        // Apply changes
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setRole(request.getRole());
        employee.setDepartment(request.getDepartment());
        employee.setJoiningDate(request.getJoiningDate());
        employee.setSalaryType(request.getSalaryType().toUpperCase());
        employee.setMonthlySalaryRate(request.getMonthlySalaryRate());
        employee.setDailyWageRate(request.getDailyWageRate());

        Employee updated = employeeRepository.save(employee);

        // Explicit Auditing
        auditLogService.log(updated.getTenantId(), "Employee", updated.getId(), "UPDATE", oldState, updated);

        return EmployeeResponse.fromEntity(updated);
    }

    @Transactional
    public void softDeleteEmployee(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));

        // Snapshot old state for auditing
        Employee oldState = new Employee();
        oldState.setId(employee.getId());
        oldState.setTenantId(employee.getTenantId());
        oldState.setFirstName(employee.getFirstName());
        oldState.setLastName(employee.getLastName());

        // Perform Soft Delete
        employee.setDeletedAt(LocalDateTime.now());
        employeeRepository.save(employee);

        // Explicit Auditing
        auditLogService.log(employee.getTenantId(), "Employee", id, "DELETE", oldState, null);
    }
}
