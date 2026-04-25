package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRiskDto {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Integer probability;

    @NotNull
    private Integer impact;

    @NotBlank
    private String level;

    private String responseStrategy;

    private Long taskId;

    private String phaseName;

    private Long ownerId;
}
