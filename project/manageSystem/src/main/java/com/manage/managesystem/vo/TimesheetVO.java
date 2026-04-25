package com.manage.managesystem.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 宸ユ椂VO
 */
@Data
public class TimesheetVO {

    private Long id;

    private Long taskId;

    private String taskName;

    private Long userId;

    private String userName;

    private LocalDate workDate;

    private BigDecimal hours;

    private BigDecimal costRate;

    private BigDecimal laborCost;

    private String description;

    private String status;
}
