package com.manage.managesystem.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 瀹為檯鎴愭湰VO
 */
@Data
public class CostActualVO {

    private Long id;

    private Long taskId;

    private String taskName;

    private String sourceType;

    private BigDecimal amount;

    private String currency;

    private LocalDate spendDate;

    private String remark;
}
