package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SaveProjectCharterDto {
    @NotBlank
    private String objective;

    private String scopeSummary;

    private String sponsor;

    private String approver;

    private String stakeholders;

    private String successCriteria;
}
