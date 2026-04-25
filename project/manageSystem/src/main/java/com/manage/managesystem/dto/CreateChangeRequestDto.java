package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateChangeRequestDto {
    @NotBlank
    private String title;

    @NotBlank
    private String type;

    @NotBlank
    private String priority;

    private String impactAnalysis;

    @NotBlank
    private String reason;
}
