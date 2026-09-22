package com.mfgops.inventory.material;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RawMaterialRepository extends JpaRepository<RawMaterial, UUID> {
    Page<RawMaterial> findAll(Pageable pageable);
    Optional<RawMaterial> findBySku(String sku);
    boolean existsBySku(String sku);

    @Query("SELECT r FROM RawMaterial r WHERE r.quantity <= r.reorderLevel")
    Page<RawMaterial> findLowStock(Pageable pageable);
}
