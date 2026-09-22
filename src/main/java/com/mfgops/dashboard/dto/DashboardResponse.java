package com.mfgops.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private String month; // YYYY-MM
    private long activeHeadcount;
    private BigDecimal monthlyPayrollCost;
    private long lowStockAlertsCount;
    private BigDecimal monthlyExpenseBurn;
    private String currency;
}
