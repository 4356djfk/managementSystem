package com.manage.managesystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaselineRecordEntity {
    private Long id;
    private Long projectId;
    private String baselineType;
    private String versionNo;
    private String baselineName;
    private String snapshotJson;
    private String status;
    private Long publishedBy;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}
