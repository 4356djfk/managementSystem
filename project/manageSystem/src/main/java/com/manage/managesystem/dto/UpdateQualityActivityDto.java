package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateQualityActivityDto {
    private Long qualityPlanId;

    @NotBlank
    private String activityName;

    @NotBlank
    private String activityType;

    private LocalDateTime plannedDate;

    private LocalDateTime actualDate;

    private String result;

    private Long ownerId;
}
