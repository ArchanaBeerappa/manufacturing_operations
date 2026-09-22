package com.mfgops.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSummaryResponse {
    private String month; // YYYY-MM
    private BigDecimal totalAmount;
    private Map<String, BigDecimal> breakdownByCategory;
    private Map<String, BigDecimal> breakdownByDepartment;
}
