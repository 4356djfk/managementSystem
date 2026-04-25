package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExportTaskVO {
    private Long id;

    private Long projectId;

    private String moduleName;

    private String exportFormat;

    private String status;

    private Long fileAttachmentId;

    private Long requestedBy;

    private LocalDateTime requestedAt;

    private LocalDateTime finishedAt;
}
