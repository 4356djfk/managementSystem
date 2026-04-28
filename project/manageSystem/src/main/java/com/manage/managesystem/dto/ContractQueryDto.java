package com.manage.managesystem.dto;

import lombok.Data;

@Data
public class ContractQueryDto {
    private String status;
    private Long supplierId;
    private String contractType;
    private Integer page = 1;
    private Integer pageSize = 10;
}
