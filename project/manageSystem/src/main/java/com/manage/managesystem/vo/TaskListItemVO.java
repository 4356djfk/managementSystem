package com.manage.managesystem.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 浠诲姟鍒楄〃椤筕O
 */
@Data
public class TaskListItemVO {

    private Long id;

    private String taskCode;

    private String name;

    private String status;

    private String priority;

    private BigDecimal progress;

    private Long assigneeId;

    private String assigneeName;

    private Long directAssigneeId;

    private String directAssigneeName;

    private LocalDateTime plannedStartDate;

    private LocalDateTime plannedEndDate;

    private LocalDateTime deadlineDate;

    private String constraintType;

    private LocalDateTime constraintDate;

    private LocalDateTime actualStartDate;

    private LocalDateTime actualEndDate;

    private BigDecimal plannedHours;

    private BigDecimal actualHours;

    private Integer sortOrder;

    private String remark;

    private String description;

    private Long parentTaskId;

    private Long milestoneId;

    private String milestoneName;

    private String taskType;
}
