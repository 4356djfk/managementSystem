package com.manage.managesystem.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProjectDashboardVO {
    private Integer taskTotal;

    private Integer taskCompleted;

    private BigDecimal taskCompletionRate;

    private BigDecimal plannedCost;

    private BigDecimal actualCost;

    private Integer openRiskCount;

    private Integer pendingChangeCount;

    private List<MilestoneVO> upcomingMilestones;
}
