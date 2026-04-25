package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRequirementDto {
    private Long wbsId;

    @NotBlank
    private String title;

    @NotBlank
    private String requirementType;

    @NotBlank
    private String priority;

    private String status;

    private String description;

    private Long proposerId;
}
