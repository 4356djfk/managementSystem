package com.manage.managesystem.dto;

import lombok.Data;

@Data
public class ProcurementQueryDto {
    private String status;
    private Long supplierId;
    private Long costPlanId;
    private Integer page = 1;
    private Integer pageSize = 10;
}
