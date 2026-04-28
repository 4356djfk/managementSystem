package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTestPlanDto {
    @NotBlank
    private String title;

    private String versionNo;

    private String scopeDesc;

    private Long ownerId;

    private String status;
}
