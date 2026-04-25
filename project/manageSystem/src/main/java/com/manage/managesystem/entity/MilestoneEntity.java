package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 閲岀▼纰戝疄锟?
 * 瀵瑰簲琛細milestone
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MilestoneEntity extends BaseEntity {
    /**
     * 椤圭洰 ID
     */
    private Long projectId;

    /**
     * 閲岀▼纰戝悕锟?
     */
    private String name;

    /**
     * 鎻忚堪
     */
    private String description;

    /**
     * 璁″垝鏃ユ湡
     */
    private LocalDateTime plannedDate;

    /**
     * 瀹為檯鏃ユ湡
     */
    private LocalDateTime actualDate;

    /**
     * 鐘舵€侊紙鍙傦拷?MilestoneStatusEnum锟?
     */
    private String status;

    /**
     * 璐熻矗锟?
     */
    private Long ownerId;
}
