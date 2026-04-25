package com.manage.managesystem.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 鎴愭湰璁″垝VO
 */
@Data
public class CostPlanVO {

    private Long id;

    private String type;

    private String name;

    private Long taskId;

    private String taskName;

    private String phase;

    private BigDecimal plannedAmount;

    private String currency;

    private String remark;
}
