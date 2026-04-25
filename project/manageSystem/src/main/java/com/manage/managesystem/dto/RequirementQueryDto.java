package com.manage.managesystem.dto;

import lombok.Data;

@Data
public class RequirementQueryDto {
    private String type;
    private String priority;
    private String status;
    private Integer page = 1;
    private Integer pageSize = 10;
}
