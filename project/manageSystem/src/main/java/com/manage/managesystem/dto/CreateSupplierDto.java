package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSupplierDto {
    @NotBlank
    private String supplierName;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String address;

    private String remark;
}
