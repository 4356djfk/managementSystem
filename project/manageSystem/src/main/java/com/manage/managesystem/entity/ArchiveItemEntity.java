package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArchiveItemEntity extends BaseEntity {
    private Long projectId;
    private String archiveType;
    private String itemName;
    private Long attachmentId;
    private String repositoryUrl;
    private String status;
    private Long archivedBy;
    private LocalDateTime archivedAt;
}
