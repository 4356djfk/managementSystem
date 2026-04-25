package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 闇€姹俈O
 */
@Data
public class RequirementVO {

    private Long id;

    private String requirementCode;

    private String title;

    private String requirementType;

    private String priority;

    private String status;

    private String description;

    private Long wbsId;

    private String wbsName;

    private Long proposerId;

    private String proposerName;

    private LocalDateTime createdAt;
}
