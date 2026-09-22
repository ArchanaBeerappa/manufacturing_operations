package com.mfgops.workforce.attendance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    Page<Attendance> findAll(Pageable pageable);
    
    Optional<Attendance> findByEmployeeIdAndCheckOutTimeIsNull(UUID employeeId);
    
    Optional<Attendance> findByEmployeeIdAndDate(UUID employeeId, LocalDate date);
    
    List<Attendance> findByEmployeeIdAndDateBetween(UUID employeeId, LocalDate start, LocalDate end);
}
