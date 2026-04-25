package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExportTaskEntity extends BaseEntity {
    private Long projectId;
    private String moduleName;
    private String exportFormat;
    private String filterJson;
    private String status;
    private Long fileAttachmentId;
    private Long requestedBy;
    private LocalDateTime requestedAt;
    private LocalDateTime finishedAt;
}
