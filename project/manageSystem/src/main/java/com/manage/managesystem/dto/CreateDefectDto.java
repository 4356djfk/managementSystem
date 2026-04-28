package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDefectDto {
    private Long testCaseId;
    private Long requirementId;
    private Long taskId;

    @NotBlank
    private String title;

    @NotBlank
    private String severity;

    @NotBlank
    private String priority;

    private String status;
    private Long reporterId;
    private Long assigneeId;
    private String description;
    private String resolution;
}
