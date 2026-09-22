package com.mfgops.inventory.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRequest {

    @NotBlank(message = "Supplier name is required")
    private String name;

    private String contactName;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    private String address;
}
