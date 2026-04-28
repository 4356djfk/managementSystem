package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateTestCaseDto {
    private Long testPlanId;
    private Long requirementId;
    private Long taskId;

    @NotBlank
    private String title;

    private String precondition;
    private String steps;
    private String expectedResult;
    private String actualResult;
    private String executionStatus;
    private Long testerId;
    private LocalDateTime executedAt;
}
