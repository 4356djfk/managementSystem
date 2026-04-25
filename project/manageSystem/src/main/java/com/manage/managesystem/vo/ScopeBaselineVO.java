package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScopeBaselineVO {
    private Long id;

    private Long projectId;

    private String versionNo;

    private String baselineName;

    private String description;

    private String snapshotJson;

    private String status;

    private Long publishedBy;

    private LocalDateTime publishedAt;
}
