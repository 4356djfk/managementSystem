package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 瀹為檯鎴愭湰瀹炰綋
 * 瀵瑰簲琛細cost_actual
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CostActualEntity extends BaseEntity {
    /**
     * 椤圭洰 ID
     */
    private Long projectId;

    /**
     * 浠诲姟 ID
     */
    private Long taskId;

    /**
     * 鎴愭湰璁″垝ID
     */
    private Long costPlanId;

    /**
     * 鏉ユ簮绫诲瀷
     */
    private String sourceType;

    /**
     * 瀹為檯閲戦
     */
    private BigDecimal actualAmount;

    /**
     * 鍙戠敓鏃ユ湡
     */
    private LocalDate occurredDate;

    /**
     * 鎻忚堪
     */
    private String description;

    /**
     * 璁板綍浜篒D
     */
    private Long recorderId;
}
