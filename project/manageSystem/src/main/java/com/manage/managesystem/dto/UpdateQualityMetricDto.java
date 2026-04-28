package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateQualityMetricDto {
    @NotBlank
    private String metricName;

    private BigDecimal metricValue;

    private String metricUnit;

    private LocalDate statisticDate;
}
