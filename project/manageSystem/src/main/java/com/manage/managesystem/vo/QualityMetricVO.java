package com.manage.managesystem.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class QualityMetricVO {
    private Long id;
    private String metricName;
    private BigDecimal metricValue;
    private String metricUnit;
    private LocalDate statisticDate;
    private LocalDateTime createdAt;
}
