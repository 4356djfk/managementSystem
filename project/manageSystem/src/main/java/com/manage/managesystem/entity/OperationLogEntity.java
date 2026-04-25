package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class OperationLogEntity extends BaseEntity {
    private Long projectId;
    private String moduleName;
    private Long bizId;
    private String action;
    private Long operatorId;
    private String operatorName;
    private String requestMethod;
    private String requestPath;
    private String requestBody;
    private Integer resultCode;
    private String ipAddress;
    private LocalDateTime createdAt;
}
