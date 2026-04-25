package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 椋庨櫓鐧昏瀹炰綋
 * 瀵瑰簲琛細risk_register
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RiskEntity extends BaseEntity {
    /**
     * 椤圭洰 ID
     */
    private Long projectId;

    /**
     * 椋庨櫓缂栧彿
     */
    private String riskCode;

    /**
     * 椋庨櫓鍚嶇О
     */
    private String name;

    /**
     * 椋庨櫓鎻忚堪
     */
    private String description;

    /**
     * 姒傜巼锟?-5
     */
    private Integer probability;

    /**
     * 褰卞搷锟?-5
     */
    private Integer impact;

    /**
     * 椋庨櫓绛夌骇锛堝弬锟?RiskLevelEnum锟?
     */
    private String level;

    /**
     * 椋庨櫓鐘舵€侊紙鍙傦拷?RiskStatusEnum锟?
     */
    private String status;

    /**
     * 搴斿绛栫暐
     */
    private String responseStrategy;

    private Long taskId;

    private String phaseName;

    /**
     * 璐ｄ换锟?
     */
    private Long ownerId;

    /**
     * 璇嗗埆鏃堕棿
     */
    private LocalDateTime identifiedAt;

    /**
     * 鍏抽棴鏃堕棿
     */
    private LocalDateTime closedAt;
}
