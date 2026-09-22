package com.mfgops.expense.dto;

import com.mfgops.expense.Expense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    private UUID id;
    private UUID tenantId;
    private String description;
    private BigDecimal amount;
    private String category;
    private String department;
    private String project;
    private String status;
    private UUID submittedById;
    private String submittedByName;
    private UUID approvedById;
    private String approvedByName;
    private UUID reviewedById;
    private String reviewedByName;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime reviewedAt;

    public static ExpenseResponse fromEntity(Expense exp) {
        UUID subById = exp.getSubmittedBy() != null ? exp.getSubmittedBy().getId() : null;
        String subByName = exp.getSubmittedBy() != null ? exp.getSubmittedBy().getUsername() : null;
        UUID appById = exp.getApprovedBy() != null ? exp.getApprovedBy().getId() : null;
        String appByName = exp.getApprovedBy() != null ? exp.getApprovedBy().getUsername() : null;
        UUID revById = exp.getReviewedBy() != null ? exp.getReviewedBy().getId() : null;
        String revByName = exp.getReviewedBy() != null ? exp.getReviewedBy().getUsername() : null;

        return ExpenseResponse.builder()
                .id(exp.getId())
                .tenantId(exp.getTenantId())
                .description(exp.getDescription())
                .amount(exp.getAmount())
                .category(exp.getCategory())
                .department(exp.getDepartment())
                .project(exp.getProject())
                .status(exp.getStatus())
                .submittedById(subById)
                .submittedByName(subByName)
                .approvedById(appById)
                .approvedByName(appByName)
                .reviewedById(revById)
                .reviewedByName(revByName)
                .submittedAt(exp.getSubmittedAt())
                .approvedAt(exp.getApprovedAt())
                .reviewedAt(exp.getReviewedAt())
                .build();
    }
}
