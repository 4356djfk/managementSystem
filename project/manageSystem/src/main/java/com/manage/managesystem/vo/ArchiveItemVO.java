package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArchiveItemVO {
    private Long id;

    private Long projectId;

    private String archiveType;

    private String itemName;

    private Long attachmentId;

    private String repositoryUrl;

    private String status;

    private Long archivedBy;

    private LocalDateTime archivedAt;
}
