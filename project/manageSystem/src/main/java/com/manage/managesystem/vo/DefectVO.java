package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DefectVO {
    private Long id;
    private Long testCaseId;
    private String testCaseTitle;
    private Long requirementId;
    private String requirementTitle;
    private Long taskId;
    private String taskName;
    private String defectCode;
    private String title;
    private String severity;
    private String priority;
    private String status;
    private Long reporterId;
    private String reporterName;
    private Long assigneeId;
    private String assigneeName;
    private String description;
    private String resolution;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
