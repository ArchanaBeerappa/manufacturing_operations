package com.mfgops.payroll.advance;

import com.mfgops.auth.User;
import com.mfgops.auth.UserRepository;
import com.mfgops.common.AuditLogService;
import com.mfgops.common.exception.ResourceNotFoundException;
import com.mfgops.workforce.employee.Employee;
import com.mfgops.workforce.employee.EmployeeRepository;
import com.mfgops.payroll.advance.dto.AdvanceRequest;
import com.mfgops.payroll.advance.dto.AdvanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdvanceSalaryService {

    private final AdvanceSalaryRepository advanceSalaryRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public AdvanceResponse requestAdvance(AdvanceRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + request.getEmployeeId()));

        AdvanceSalary adv = new AdvanceSalary();
        adv.setEmployee(employee);
        adv.setAmount(request.getAmount());
        adv.setRequestDate(LocalDate.now());
        adv.setDeductionMonth(request.getDeductionMonth());
        adv.setStatus("PENDING");

        AdvanceSalary saved = advanceSalaryRepository.save(adv);

        // Explicit Auditing
        auditLogService.log(saved.getTenantId(), "AdvanceSalary", saved.getId(), "CREATE", null, saved);

        return AdvanceResponse.fromEntity(saved);
    }

    @Transactional
    public AdvanceResponse approveAdvance(UUID id, String status) {
        if (!"APPROVED".equalsIgnoreCase(status) && !"REJECTED".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Invalid status update. Must be APPROVED or REJECTED.");
        }

        AdvanceSalary adv = advanceSalaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Advance request not found: " + id));

        if (!"PENDING".equalsIgnoreCase(adv.getStatus())) {
            throw new IllegalStateException("Advance request has already been processed: " + adv.getStatus());
        }

        // Fetch Approver Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User approver = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            approver = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("Approver user context not found: " + authentication.getName()));
        }

        // Snapshot old state for auditing
        AdvanceSalary oldState = new AdvanceSalary();
        oldState.setId(adv.getId());
        oldState.setTenantId(adv.getTenantId());
        oldState.setEmployee(adv.getEmployee());
        oldState.setAmount(adv.getAmount());
        oldState.setRequestDate(adv.getRequestDate());
        oldState.setStatus(adv.getStatus());
        oldState.setDeductionMonth(adv.getDeductionMonth());

        // Apply state transition
        adv.setStatus(status.toUpperCase());
        adv.setApprovedBy(approver);

        AdvanceSalary saved = advanceSalaryRepository.save(adv);

        // Explicit Auditing
        auditLogService.log(saved.getTenantId(), "AdvanceSalary", saved.getId(), "APPROVE", oldState, saved);

        return AdvanceResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<AdvanceResponse> getAdvancesByEmployeeId(UUID employeeId, Pageable pageable) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found: " + employeeId);
        }
        return advanceSalaryRepository.findByEmployeeId(employeeId, pageable)
                .map(AdvanceResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<AdvanceResponse> getAllAdvances(Pageable pageable) {
        return advanceSalaryRepository.findAll(pageable)
                .map(AdvanceResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public AdvanceResponse getAdvanceById(UUID id) {
        AdvanceSalary adv = advanceSalaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Advance request not found: " + id));
        return AdvanceResponse.fromEntity(adv);
    }
}
