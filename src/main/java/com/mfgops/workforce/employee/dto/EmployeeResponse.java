package com.mfgops.workforce.employee.dto;

import com.mfgops.workforce.employee.Employee;
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
public class EmployeeResponse {
    private UUID id;
    private UUID tenantId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String role;
    private String department;
    private LocalDate joiningDate;
    private String salaryType;
    private BigDecimal monthlySalaryRate;
    private BigDecimal dailyWageRate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EmployeeResponse fromEntity(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .tenantId(employee.getTenantId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .role(employee.getRole())
                .department(employee.getDepartment())
                .joiningDate(employee.getJoiningDate())
                .salaryType(employee.getSalaryType())
                .monthlySalaryRate(employee.getMonthlySalaryRate())
                .dailyWageRate(employee.getDailyWageRate())
                .status(employee.getStatus())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }
}
