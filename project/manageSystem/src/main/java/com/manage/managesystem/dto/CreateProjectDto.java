package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateProjectDto {
    @NotBlank
    private String name;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    @NotBlank
    private String lifeCycleModel;

    private Long ownerId;

    private Long templateId;

    private BigDecimal plannedBudget;
}
