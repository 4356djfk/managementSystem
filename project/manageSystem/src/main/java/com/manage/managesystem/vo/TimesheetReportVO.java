package com.manage.managesystem.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TimesheetReportVO {
    private BigDecimal totalHours;

    private BigDecimal totalLaborCost;

    private Integer userCount;

    private List<TimesheetVO> records;
}
