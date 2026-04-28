package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class TestCaseEntity extends BaseEntity {
    private Long projectId;
    private Long testPlanId;
    private Long requirementId;
    private Long taskId;
    private String caseCode;
    private String title;
    private String precondition;
    private String steps;
    private String expectedResult;
    private String actualResult;
    private String executionStatus;
    private Long testerId;
    private LocalDateTime executedAt;
}
