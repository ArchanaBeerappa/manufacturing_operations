package com.mfgops.expense;

import com.mfgops.auth.User;
import com.mfgops.auth.UserRepository;
import com.mfgops.common.AuditLogService;
import com.mfgops.common.exception.ResourceNotFoundException;
import com.mfgops.expense.dto.ExpenseRequest;
import com.mfgops.expense.dto.ExpenseResponse;
import com.mfgops.expense.dto.ExpenseSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public ExpenseResponse submitExpense(ExpenseRequest request) {
        // Resolve submitter context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Current user context not found: " + authentication.getName()));

        Expense exp = new Expense();
        exp.setDescription(request.getDescription());
        exp.setAmount(request.getAmount());
        exp.setCategory(request.getCategory().toUpperCase());
        exp.setDepartment(request.getDepartment());
        exp.setProject(request.getProject());
        exp.setStatus("SUBMITTED");
        exp.setSubmittedBy(currentUser);
        exp.setSubmittedAt(LocalDateTime.now());

        Expense saved = expenseRepository.save(exp);

        // Explicit Auditing
        auditLogService.log(saved.getTenantId(), "Expense", saved.getId(), "CREATE", null, saved);

        return ExpenseResponse.fromEntity(saved);
    }

    @Transactional
    public ExpenseResponse approveExpense(UUID id) {
        Expense exp = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));

        if (!"SUBMITTED".equalsIgnoreCase(exp.getStatus())) {
            throw new IllegalStateException("Only SUBMITTED expenses can be approved. Current status: " + exp.getStatus());
        }

        // Resolve manager context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Approver user context not found: " + authentication.getName()));

        // Snapshot old state for auditing
        Expense oldState = new Expense();
        oldState.setId(exp.getId());
        oldState.setTenantId(exp.getTenantId());
        oldState.setStatus(exp.getStatus());
        oldState.setAmount(exp.getAmount());

        // Update status
        exp.setStatus("APPROVED");
        exp.setApprovedBy(currentUser);
        exp.setApprovedAt(LocalDateTime.now());

        Expense saved = expenseRepository.save(exp);

        // Explicit Auditing
        auditLogService.log(saved.getTenantId(), "Expense", saved.getId(), "APPROVE", oldState, saved);

        return ExpenseResponse.fromEntity(saved);
    }

    @Transactional
    public ExpenseResponse reviewExpense(UUID id) {
        Expense exp = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));

        if (!"APPROVED".equalsIgnoreCase(exp.getStatus())) {
            throw new IllegalStateException("Only APPROVED expenses can be reviewed. Current status: " + exp.getStatus());
        }

        // Resolve owner context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Reviewer user context not found: " + authentication.getName()));

        if (!"OWNER".equalsIgnoreCase(currentUser.getRole())) {
            throw new IllegalArgumentException("Only the OWNER can execute the final review on expenses.");
        }

        // Snapshot old state
        Expense oldState = new Expense();
        oldState.setId(exp.getId());
        oldState.setTenantId(exp.getTenantId());
        oldState.setStatus(exp.getStatus());
        oldState.setAmount(exp.getAmount());

        // Update status
        exp.setStatus("REVIEWED");
        exp.setReviewedBy(currentUser);
        exp.setReviewedAt(LocalDateTime.now());

        Expense saved = expenseRepository.save(exp);

        // Explicit Auditing
        auditLogService.log(saved.getTenantId(), "Expense", saved.getId(), "REVIEW", oldState, saved);

        return ExpenseResponse.fromEntity(saved);
    }

    @Transactional
    public ExpenseResponse rejectExpense(UUID id) {
        Expense exp = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));

        if (!"SUBMITTED".equalsIgnoreCase(exp.getStatus()) && !"APPROVED".equalsIgnoreCase(exp.getStatus())) {
            throw new IllegalStateException("Only SUBMITTED or APPROVED expenses can be rejected. Current status: " + exp.getStatus());
        }

        // Snapshot old state
        Expense oldState = new Expense();
        oldState.setId(exp.getId());
        oldState.setTenantId(exp.getTenantId());
        oldState.setStatus(exp.getStatus());

        exp.setStatus("REJECTED");
        Expense saved = expenseRepository.save(exp);

        // Explicit Auditing
        auditLogService.log(saved.getTenantId(), "Expense", saved.getId(), "REJECT", oldState, saved);

        return ExpenseResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getAllExpenses(Pageable pageable) {
        return expenseRepository.findAll(pageable)
                .map(ExpenseResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(UUID id) {
        Expense exp = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense record not found: " + id));
        return ExpenseResponse.fromEntity(exp);
    }

    @Transactional(readOnly = true)
    public ExpenseSummaryResponse getMonthlySummary(String month) {
        YearMonth ym = YearMonth.parse(month); // E.g., YYYY-MM
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59, 999999999);

        List<Expense> expenses = expenseRepository.findBySubmittedAtBetween(start, end);

        BigDecimal total = BigDecimal.ZERO;
        Map<String, BigDecimal> categorySums = new HashMap<>();
        Map<String, BigDecimal> deptSums = new HashMap<>();

        // Group and sum amounts
        for (Expense exp : expenses) {
            // Count only approved, reviewed, or submitted operational logs (exclude rejected ones)
            if ("REJECTED".equalsIgnoreCase(exp.getStatus())) {
                continue;
            }

            BigDecimal amt = exp.getAmount();
            total = total.add(amt);

            String cat = exp.getCategory().toUpperCase();
            categorySums.put(cat, categorySums.getOrDefault(cat, BigDecimal.ZERO).add(amt));

            String dept = exp.getDepartment();
            if (dept != null && !dept.isBlank()) {
                dept = dept.toUpperCase();
                deptSums.put(dept, deptSums.getOrDefault(dept, BigDecimal.ZERO).add(amt));
            }
        }

        return ExpenseSummaryResponse.builder()
                .month(month)
                .totalAmount(total)
                .breakdownByCategory(categorySums)
                .breakdownByDepartment(deptSums)
                .build();
    }
}
