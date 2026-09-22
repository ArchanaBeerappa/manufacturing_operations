package com.mfgops.expense;

import com.mfgops.common.StandardResponse;
import com.mfgops.expense.dto.ExpenseRequest;
import com.mfgops.expense.dto.ExpenseResponse;
import com.mfgops.expense.dto.ExpenseSummaryResponse;
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
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<ExpenseResponse>> submitExpense(@Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.submitExpense(request);
        return ResponseEntity.ok(StandardResponse.success(response, "Expense submitted successfully."));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<ExpenseResponse>> approveExpense(@PathVariable UUID id) {
        ExpenseResponse response = expenseService.approveExpense(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Expense approved successfully."));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('OWNER')")
    public ResponseEntity<StandardResponse<ExpenseResponse>> reviewExpense(@PathVariable UUID id) {
        ExpenseResponse response = expenseService.reviewExpense(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Expense reviewed and completed successfully."));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<ExpenseResponse>> rejectExpense(@PathVariable UUID id) {
        ExpenseResponse response = expenseService.rejectExpense(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Expense rejected successfully."));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<StandardResponse<ExpenseSummaryResponse>> getMonthlySummary(@RequestParam String month) {
        ExpenseSummaryResponse response = expenseService.getMonthlySummary(month);
        return ResponseEntity.ok(StandardResponse.success(response, "Monthly expense summary retrieved successfully."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<ExpenseResponse>> getExpenseById(@PathVariable UUID id) {
        ExpenseResponse response = expenseService.getExpenseById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Expense record retrieved successfully."));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<Page<ExpenseResponse>>> getAllExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<ExpenseResponse> expenses = expenseService.getAllExpenses(pageRequest);
        return ResponseEntity.ok(StandardResponse.success(expenses, "Expenses list retrieved successfully."));
    }
}
