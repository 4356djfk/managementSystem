package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateCostPlanDto {
    @NotBlank
    private String type;

    @NotBlank
    private String name;

    private Long taskId;

    private String phase;

    @NotNull
    private BigDecimal plannedAmount;

    @NotBlank
    private String currency;

    private String remark;
}
