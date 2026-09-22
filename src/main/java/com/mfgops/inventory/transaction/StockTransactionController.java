package com.mfgops.inventory.transaction;

import com.mfgops.common.StandardResponse;
import com.mfgops.inventory.transaction.dto.StockTransactionRequest;
import com.mfgops.inventory.transaction.dto.StockTransactionResponse;
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
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class StockTransactionController {

    private final StockTransactionService stockTransactionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<StockTransactionResponse>> performTransaction(
            @Valid @RequestBody StockTransactionRequest request) {
        StockTransactionResponse response = stockTransactionService.performTransaction(request);
        return ResponseEntity.ok(StandardResponse.success(response, "Stock transaction processed successfully."));
    }

    @GetMapping("/material/{materialId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<Page<StockTransactionResponse>>> getTransactionsByMaterialId(
            @PathVariable UUID materialId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<StockTransactionResponse> history = stockTransactionService.getTransactionsByMaterialId(materialId, pageRequest);
        return ResponseEntity.ok(StandardResponse.success(history, "Stock transactions history retrieved successfully."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'FLOOR_STAFF')")
    public ResponseEntity<StandardResponse<StockTransactionResponse>> getTransactionById(@PathVariable UUID id) {
        StockTransactionResponse response = stockTransactionService.getTransactionById(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Stock transaction retrieved successfully."));
    }
}
