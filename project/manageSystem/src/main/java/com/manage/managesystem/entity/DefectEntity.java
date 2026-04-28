package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DefectEntity extends BaseEntity {
    private Long projectId;
    private Long testCaseId;
    private Long requirementId;
    private Long taskId;
    private String defectCode;
    private String title;
    private String severity;
    private String priority;
    private String status;
    private Long reporterId;
    private Long assigneeId;
    private String description;
    private String resolution;
}
