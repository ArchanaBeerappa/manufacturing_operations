package com.mfgops.inventory.transaction;

import com.mfgops.auth.User;
import com.mfgops.auth.UserRepository;
import com.mfgops.common.AuditLogService;
import com.mfgops.common.exception.ResourceNotFoundException;
import com.mfgops.inventory.material.RawMaterial;
import com.mfgops.inventory.material.RawMaterialRepository;
import com.mfgops.inventory.transaction.dto.StockTransactionRequest;
import com.mfgops.inventory.transaction.dto.StockTransactionResponse;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockTransactionService {

    private final StockTransactionRepository stockTransactionRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public StockTransactionResponse performTransaction(StockTransactionRequest request) {
        // 1. Fetch Raw Material
        RawMaterial material = rawMaterialRepository.findById(request.getMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException("Raw material not found: " + request.getMaterialId()));

        // Snapshot raw material state for audit log before updating quantity
        RawMaterial oldMaterialState = new RawMaterial();
        oldMaterialState.setId(material.getId());
        oldMaterialState.setTenantId(material.getTenantId());
        oldMaterialState.setSku(material.getSku());
        oldMaterialState.setName(material.getName());
        oldMaterialState.setQuantity(material.getQuantity());
        oldMaterialState.setReorderLevel(material.getReorderLevel());

        // 2. Resolve Authenticated User
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            currentUser = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("Current user context not found in DB: " + authentication.getName()));
        }

        // 3. Perform Arithmetic and Business Rules
        BigDecimal originalQuantity = material.getQuantity();
        BigDecimal txnQuantity = request.getQuantity();
        BigDecimal newQuantity;

        if ("STOCK_IN".equalsIgnoreCase(request.getTransactionType())) {
            newQuantity = originalQuantity.add(txnQuantity);
        } else if ("STOCK_OUT".equalsIgnoreCase(request.getTransactionType())) {
            if (originalQuantity.compareTo(txnQuantity) < 0) {
                throw new IllegalArgumentException("Insufficient stock! Available: " 
                        + originalQuantity + " " + material.getUnit() 
                        + ", Requested: " + txnQuantity + " " + material.getUnit());
            }
            newQuantity = originalQuantity.subtract(txnQuantity);
        } else {
            throw new IllegalArgumentException("Unsupported transaction type: " + request.getTransactionType());
        }

        // Update raw material quantity
        material.setQuantity(newQuantity);
        RawMaterial updatedMaterial = rawMaterialRepository.save(material);

        // 4. Save Stock Transaction Log
        StockTransaction tx = new StockTransaction();
        tx.setMaterial(updatedMaterial);
        tx.setTransactionType(request.getTransactionType().toUpperCase());
        tx.setQuantity(txnQuantity);
        tx.setRemarks(request.getRemarks());
        tx.setUser(currentUser);
        tx.setTimestamp(LocalDateTime.now());

        StockTransaction savedTx = stockTransactionRepository.save(tx);

        // 5. Explicit Dual Auditing
        // Audit 1: Create transaction record
        auditLogService.log(savedTx.getTenantId(), "StockTransaction", savedTx.getId(), "CREATE", null, savedTx);
        // Audit 2: Track material quantity modification
        auditLogService.log(updatedMaterial.getTenantId(), "RawMaterial", updatedMaterial.getId(), "STOCK_MUTATION", oldMaterialState, updatedMaterial);

        return StockTransactionResponse.fromEntity(savedTx);
    }

    @Transactional(readOnly = true)
    public Page<StockTransactionResponse> getTransactionsByMaterialId(UUID materialId, Pageable pageable) {
        // Assert raw material exists
        if (!rawMaterialRepository.existsById(materialId)) {
            throw new ResourceNotFoundException("Raw material not found: " + materialId);
        }
        return stockTransactionRepository.findByMaterialId(materialId, pageable)
                .map(StockTransactionResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public StockTransactionResponse getTransactionById(UUID id) {
        StockTransaction tx = stockTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock transaction not found: " + id));
        return StockTransactionResponse.fromEntity(tx);
    }
}
