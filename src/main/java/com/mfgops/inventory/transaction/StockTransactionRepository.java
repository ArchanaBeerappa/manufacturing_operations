package com.mfgops.inventory.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, UUID> {
    Page<StockTransaction> findByMaterialId(UUID materialId, Pageable pageable);
}
