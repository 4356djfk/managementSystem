package com.manage.managesystem.vo;

import lombok.Data;

/**
 * 浠诲姟渚濊禆VO
 */
@Data
public class TaskDependencyVO {

    private Long id;

    private Long predecessorTaskId;

    private String predecessorTaskName;

    private Long successorTaskId;

    private String successorTaskName;

    private String dependencyType;

    private Integer lagDays;
}
