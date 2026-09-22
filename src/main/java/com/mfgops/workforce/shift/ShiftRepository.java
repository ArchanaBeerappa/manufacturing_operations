package com.mfgops.workforce.shift;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, UUID> {
    Page<Shift> findAll(Pageable pageable);
    Optional<Shift> findByNameIgnoreCase(String name);
}
