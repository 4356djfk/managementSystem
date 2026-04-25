package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 鎴愭湰璁″垝瀹炰綋
 * 瀵瑰簲琛細cost_plan
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CostPlanEntity extends BaseEntity {
    /**
     * 椤圭洰 ID
     */
    private Long projectId;

    /**
     * 鎴愭湰绫诲瀷
     */
    private String type;

    /**
     * 璁″垝鍚嶇О
     */
    private String name;

    /**
     * 鍏宠仈浠诲姟 ID
     */
    private Long taskId;

    /**
     * 鎵€灞為樁锟?
     */
    private String phase;

    /**
     * 璁″垝閲戦
     */
    private BigDecimal plannedAmount;

    /**
     * 甯佺
     */
    private String currency;

    /**
     * 鍩虹嚎鐗堟湰
     */
    private String baselineVersion;

    /**
     * 澶囨敞
     */
    private String remark;
}
