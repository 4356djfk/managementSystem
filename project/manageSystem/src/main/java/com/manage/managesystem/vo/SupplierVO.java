package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierVO {
    private Long id;
    private String supplierName;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String address;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
