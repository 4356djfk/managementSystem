package com.manage.managesystem.dto;

import lombok.Data;

@Data
public class TestPlanQueryDto {
    private String status;
    private Long ownerId;
    private Integer page = 1;
    private Integer pageSize = 10;
}
