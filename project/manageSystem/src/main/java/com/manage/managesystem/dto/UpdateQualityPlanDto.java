package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateQualityPlanDto {
    @NotBlank
    private String title;

    private String qualityStandard;

    private String acceptanceRule;

    private Long ownerId;

    private String status;
}
