package com.manage.managesystem.dto;

import lombok.Data;

@Data
public class SupplierQueryDto {
    private String keyword;
    private Integer page = 1;
    private Integer pageSize = 10;
}
