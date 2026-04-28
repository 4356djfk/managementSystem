package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConfigItemVO {
    private Long id;
    private Long projectId;
    private String itemName;
    private String itemType;
    private String versionNo;
    private String status;
    private String repositoryUrl;
    private String remark;
    private Long createdBy;
    private String createdByName;
    private Long updatedBy;
    private String updatedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
