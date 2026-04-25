package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 鍙樻洿璇锋眰瀹炰綋
 * 瀵瑰簲琛細change_request
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChangeRequestEntity extends BaseEntity {
    /**
     * 椤圭洰 ID
     */
    private Long projectId;

    /**
     * 鍙樻洿鍗曞彿
     */
    private String changeCode;

    /**
     * 鏍囬
     */
    private String title;

    /**
     * 鍙樻洿绫诲瀷锛堝弬锟?ChangeTypeEnum锟?
     */
    private String changeType;

    /**
     * 浼樺厛绾э紙鍙傦拷?PriorityEnum锟?
     */
    private String priority;

    /**
     * 鐘舵€侊紙鍙傦拷?ChangeStatusEnum锟?
     */
    private String status;

    /**
     * 鎻愬嚭锟?
     */
    private Long proposerId;

    /**
     * 瀹℃壒锟?
     */
    private Long approverId;

    /**
     * 鍙樻洿鍘熷洜
     */
    private String reason;

    /**
     * 褰卞搷鍒嗘瀽
     */
    private String impactAnalysis;

    /**
     * 瀹℃壒鎰忚
     */
    private String decisionComment;

    /**
     * 鎻愪氦鏃堕棿
     */
    private LocalDateTime submittedAt;

    /**
     * 瀹℃壒鏃堕棿
     */
    private LocalDateTime decidedAt;

    /**
     * 钀藉湴鏃堕棿
     */
    private LocalDateTime implementedAt;
}
