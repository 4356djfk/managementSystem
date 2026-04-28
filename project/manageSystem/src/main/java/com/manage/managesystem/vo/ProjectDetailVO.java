package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 椤圭洰璇︽儏VO
 */
@Data
public class ProjectDetailVO extends ProjectListItemVO {

    private LocalDate actualStartDate;

    private LocalDate actualEndDate;

    private Long templateId;

    private String templateName;

    private Boolean isDemo;

    private Integer openRiskCount;

    private Integer pendingChangeCount;
}
