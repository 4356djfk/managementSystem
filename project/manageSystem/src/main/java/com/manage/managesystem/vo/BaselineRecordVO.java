package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaselineRecordVO {
    private Long id;
    private Long projectId;
    private String baselineType;
    private String versionNo;
    private String baselineName;
    private String description;
    private String snapshotJson;
    private String status;
    private Long publishedBy;
    private String publishedByName;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}
