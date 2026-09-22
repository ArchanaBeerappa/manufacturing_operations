package com.mfgops.inventory.supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    Page<Supplier> findAll(Pageable pageable);
    Optional<Supplier> findByName(String name);
    boolean existsByName(String name);
}
