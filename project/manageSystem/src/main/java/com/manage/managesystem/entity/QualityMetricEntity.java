package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class QualityMetricEntity extends BaseEntity {
    private Long projectId;
    private String metricName;
    private BigDecimal metricValue;
    private String metricUnit;
    private LocalDate statisticDate;
}
