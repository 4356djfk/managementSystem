package com.manage.managesystem.dto;

import lombok.Data;

@Data
public class QualityActivityQueryDto {
    private Long qualityPlanId;
    private String activityType;
    private Long ownerId;
    private Integer page = 1;
    private Integer pageSize = 10;
}
