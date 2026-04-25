package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectTemplateTaskEntity extends BaseEntity {
    private Long templateId;
    private Long parentId;
    private String name;
    private String taskType;
    private Integer defaultDays;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
