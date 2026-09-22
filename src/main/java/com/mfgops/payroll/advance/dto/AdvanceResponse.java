package com.mfgops.payroll.advance.dto;

import com.mfgops.payroll.advance.AdvanceSalary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvanceResponse {
    private UUID id;
    private UUID tenantId;
    private UUID employeeId;
    private String employeeName;
    private BigDecimal amount;
    private LocalDate requestDate;
    private String status;
    private UUID approvedById;
    private String approvedByName;
    private String deductionMonth;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdvanceResponse fromEntity(AdvanceSalary adv) {
        UUID appById = adv.getApprovedBy() != null ? adv.getApprovedBy().getId() : null;
        String appByName = adv.getApprovedBy() != null ? adv.getApprovedBy().getUsername() : null;

        return AdvanceResponse.builder()
                .id(adv.getId())
                .tenantId(adv.getTenantId())
                .employeeId(adv.getEmployee().getId())
                .employeeName(adv.getEmployee().getFirstName() + " " + adv.getEmployee().getLastName())
                .amount(adv.getAmount())
                .requestDate(adv.getRequestDate())
                .status(adv.getStatus())
                .approvedById(appById)
                .approvedByName(appByName)
                .deductionMonth(adv.getDeductionMonth())
                .createdAt(adv.getCreatedAt())
                .updatedAt(adv.getUpdatedAt())
                .build();
    }
}
