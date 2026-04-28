package com.manage.managesystem.dto;

import lombok.Data;

@Data
public class ConfigItemQueryDto {
    private String keyword;
    private String itemType;
    private String status;
    private Integer page = 1;
    private Integer pageSize = 10;
}
