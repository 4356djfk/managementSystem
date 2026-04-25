package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 椤圭洰淇℃伅瀹炰綋
 * 瀵瑰簲琛細project_info
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectEntity extends BaseEntity {
    /**
     * 椤圭洰缂栫爜
     */
    private String projectCode;

    /**
     * 椤圭洰鍚嶇О
     */
    private String name;

    /**
     * 椤圭洰鎻忚堪
     */
    private String description;

    /**
     * 鐢熷懡鍛ㄦ湡妯″瀷锛堝弬锟?LifeCycleModelEnum锟?
     */
    private String lifeCycleModel;

    /**
     * 椤圭洰鐘舵€侊紙鍙傦拷?ProjectStatusEnum锟?
     */
    private String status;

    /**
     * 璁″垝寮€濮嬫棩锟?
     */
    private LocalDate startDate;

    /**
     * 璁″垝缁撴潫鏃ユ湡
     */
    private LocalDate endDate;

    /**
     * 瀹為檯寮€濮嬫棩锟?
     */
    private LocalDate actualStartDate;

    /**
     * 瀹為檯缁撴潫鏃ユ湡
     */
    private LocalDate actualEndDate;

    /**
     * 椤圭洰缁忕悊 ID
     */
    private Long ownerId;

    /**
     * 妯℃澘 ID
     */
    private Long templateId;

    /**
     * 椤圭洰杩涘害
     */
    private BigDecimal progressRate;

    /**
     * 璁″垝棰勭畻
     */
    private BigDecimal plannedBudget;

    /**
     * 瀹為檯鎴愭湰
     */
    private BigDecimal actualCost;

    /**
     * 鏄惁婕旂ず椤圭洰
     */
    private Integer isDemo;
}
