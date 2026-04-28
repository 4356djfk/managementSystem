package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestCaseVO {
    private Long id;
    private Long testPlanId;
    private String testPlanTitle;
    private Long requirementId;
    private String requirementTitle;
    private Long taskId;
    private String taskName;
    private String caseCode;
    private String title;
    private String precondition;
    private String steps;
    private String expectedResult;
    private String actualResult;
    private String executionStatus;
    private Long testerId;
    private String testerName;
    private LocalDateTime executedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
