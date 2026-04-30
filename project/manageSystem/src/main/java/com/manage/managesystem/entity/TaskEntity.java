package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 椤圭洰浠诲姟瀹炰綋
 * 瀵瑰簲琛細project_task
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskEntity extends BaseEntity {
    /**
     * 椤圭洰 ID
     */
    private Long projectId;

    /**
     * 鐖朵换锟?ID
     */
    private Long parentTaskId;

    /**
     * WBS ID
     */
    private Long wbsId;

    /**
     * 閲岀▼锟?ID
     */
    private Long milestoneId;

    /**
     * 浠诲姟缂栧彿
     */
    private String taskCode;

    /**
     * 浠诲姟鍚嶇О
     */
    private String name;

    /**
     * 浠诲姟鎻忚堪
     */
    private String description;

    /**
     * 浠诲姟绫诲瀷锛堝弬锟?TaskTypeEnum锟?
     */
    private String taskType;

    /**
     * 浼樺厛绾э紙鍙傦拷?PriorityEnum锟?
     */
    private String priority;

    /**
     * 浠诲姟鐘舵€侊紙鍙傦拷?TaskStatusEnum锟?
     */
    private String status;

    /**
     * 杩涘害鐧惧垎锟?
     */
    private BigDecimal progress;

    /**
     * 璐熻矗锟?
     */
    private Long assigneeId;

    /**
     * 璁″垝寮€濮嬫椂锟?
     */
    private LocalDateTime plannedStartDate;

    /**
     * 璁″垝缁撴潫鏃堕棿
     */
    private LocalDateTime plannedEndDate;

    private LocalDateTime deadlineDate;

    private String constraintType;

    private LocalDateTime constraintDate;

    /**
     * 瀹為檯寮€濮嬫椂锟?
     */
    private LocalDateTime actualStartDate;

    /**
     * 瀹為檯缁撴潫鏃堕棿
     */
    private LocalDateTime actualEndDate;

    /**
     * 璁″垝宸ユ椂
     */
    private BigDecimal plannedHours;

    /**
     * 瀹為檯宸ユ椂
     */
    private BigDecimal actualHours;

    /**
     * 璁″垝鎴愭湰
     */
    private BigDecimal plannedCost;

    /**
     * 瀹為檯鎴愭湰
     */
    private BigDecimal actualCost;

    /**
     * 鎺掑簭
     */
    private Integer sortOrder;

    /**
     * 澶囨敞
     */
    private String remark;
}
