package com.manage.managesystem.dto;

import lombok.Data;

@Data
public class TestCaseQueryDto {
    private Long testPlanId;
    private String executionStatus;
    private Long testerId;
    private Integer page = 1;
    private Integer pageSize = 10;
}
