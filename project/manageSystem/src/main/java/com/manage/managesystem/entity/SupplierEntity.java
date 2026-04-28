package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SupplierEntity extends BaseEntity {
    private Long projectId;
    private String supplierName;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String address;
    private String remark;
}
