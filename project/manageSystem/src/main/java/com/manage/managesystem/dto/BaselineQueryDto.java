package com.manage.managesystem.dto;

import lombok.Data;

@Data
public class BaselineQueryDto {
    private String baselineType;
    private String status;
    private Integer page = 1;
    private Integer pageSize = 10;
}
