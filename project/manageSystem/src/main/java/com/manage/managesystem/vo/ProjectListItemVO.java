package com.manage.managesystem.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 椤圭洰鍒楄〃椤筕O
 */
@Data
public class ProjectListItemVO {

    private Long id;

    private String projectCode;

    private String name;

    private String description;

    private String status;

    private String lifeCycleModel;

    private Long ownerId;

    private String ownerName;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal progressRate;

    private BigDecimal plannedBudget;

    private BigDecimal actualCost;
}
