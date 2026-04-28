package com.manage.managesystem.dto;

import lombok.Data;

@Data
public class QualityMetricQueryDto {
    private String metricName;
    private Integer page = 1;
    private Integer pageSize = 10;
}
