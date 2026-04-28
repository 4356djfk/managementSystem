package com.manage.managesystem.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AuditLogQueryDto {
    private Long projectId;
    private String module;
    private Long operatorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer page;
    private Integer pageSize;
    private List<String> allowedModules;
    private Boolean businessOperatorsOnly;
}
