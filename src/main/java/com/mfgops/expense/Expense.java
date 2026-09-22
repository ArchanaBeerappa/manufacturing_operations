package com.mfgops.expense;

import com.mfgops.auth.User;
import com.mfgops.common.TenantBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "expenses")
public class Expense extends TenantBaseEntity {

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "category", nullable = false)
    private String category; // UTILITIES, MAINTENANCE, TRANSPORT, MISC

    @Column(name = "department")
    private String department;

    @Column(name = "project")
    private String project;

    @Column(name = "status", nullable = false)
    private String status = "SUBMITTED"; // SUBMITTED, APPROVED, REVIEWED, REJECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
