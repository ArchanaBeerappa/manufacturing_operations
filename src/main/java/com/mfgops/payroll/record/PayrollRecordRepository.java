package com.mfgops.payroll.record;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, UUID> {
    Page<PayrollRecord> findAll(Pageable pageable);
    
    Optional<PayrollRecord> findByEmployeeIdAndPayrollMonth(UUID employeeId, String payrollMonth);
    
    Page<PayrollRecord> findByEmployeeId(UUID employeeId, Pageable pageable);
}
