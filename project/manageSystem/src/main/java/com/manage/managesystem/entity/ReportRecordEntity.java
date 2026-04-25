package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReportRecordEntity extends BaseEntity {
    private Long projectId;
    private String reportType;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String contentJson;
    private Long attachmentId;
    private Long generatedBy;
    private LocalDateTime generatedAt;
    private String status;
}
