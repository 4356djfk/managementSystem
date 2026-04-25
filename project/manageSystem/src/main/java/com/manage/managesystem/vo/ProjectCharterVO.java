package com.manage.managesystem.vo;

import lombok.Data;

/**
 * 椤圭洰绔犵▼VO
 */
@Data
public class ProjectCharterVO {

    private Long id;

    private Long projectId;

    private String projectName;

    private String objective;

    private String scopeSummary;

    private String sponsor;

    private String approver;

    private String stakeholders;

    private String successCriteria;
}
