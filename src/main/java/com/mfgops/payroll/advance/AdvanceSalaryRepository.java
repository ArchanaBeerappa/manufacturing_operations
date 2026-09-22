package com.mfgops.payroll.advance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdvanceSalaryRepository extends JpaRepository<AdvanceSalary, UUID> {
    Page<AdvanceSalary> findAll(Pageable pageable);
    
    List<AdvanceSalary> findByEmployeeIdAndDeductionMonthAndStatus(UUID employeeId, String deductionMonth, String status);
    
    Page<AdvanceSalary> findByEmployeeId(UUID employeeId, Pageable pageable);
}
