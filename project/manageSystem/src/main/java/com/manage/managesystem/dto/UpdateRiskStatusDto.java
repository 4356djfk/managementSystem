package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRiskStatusDto {
    @NotBlank
    private String status;

    private String comment;
}
