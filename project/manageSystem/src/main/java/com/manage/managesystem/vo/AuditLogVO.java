package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogVO {
    private Long id;

    private Long projectId;

    private String moduleName;

    private Long bizId;

    private String action;

    private Long operatorId;

    private String operatorName;

    private String requestMethod;

    private String requestPath;

    private Integer resultCode;

    private String ipAddress;

    private LocalDateTime createdAt;
}
