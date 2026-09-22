package com.mfgops.workforce.attendance;

import com.mfgops.auth.User;
import com.mfgops.common.TenantBaseEntity;
import com.mfgops.workforce.employee.Employee;
import com.mfgops.workforce.shift.Shift;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "attendances", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "employee_id", "date"})
})
public class Attendance extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private Shift shift;

    @Column(name = "status", nullable = false)
    private String status = "PRESENT"; // PRESENT, ABSENT, HALF_DAY, LATE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corrected_by")
    private User correctedBy;

    @Column(name = "correction_reason", columnDefinition = "TEXT")
    private String correctionReason;

    @Column(name = "corrected_at")
    private LocalDateTime correctedAt;
}
