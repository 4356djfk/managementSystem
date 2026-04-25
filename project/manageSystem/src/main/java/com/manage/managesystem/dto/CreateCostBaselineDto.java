package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCostBaselineDto {
    @NotBlank
    private String baselineName;

    private String description;

    private String snapshotJson;
}
