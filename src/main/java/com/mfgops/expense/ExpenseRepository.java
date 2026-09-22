package com.mfgops.expense;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    Page<Expense> findAll(Pageable pageable);
    
    List<Expense> findBySubmittedAtBetween(LocalDateTime start, LocalDateTime end);
}
