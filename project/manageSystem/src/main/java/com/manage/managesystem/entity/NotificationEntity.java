package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 閫氱煡瀹炰綋
 * 瀵瑰簲琛細notification
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationEntity extends BaseEntity {
    /**
     * 鎺ユ敹锟?
     */
    private Long receiverId;

    /**
     * 鍏宠仈椤圭洰
     */
    private Long projectId;

    /**
     * 涓氬姟绫诲瀷
     */
    private String bizType;

    /**
     * 涓氬姟 ID
     */
    private Long bizId;

    /**
     * 鏍囬
     */
    private String title;

    /**
     * 鍐呭
     */
    private String content;

    /**
     * 宸茶鐘舵€侊紙鍙傦拷?NotificationReadStatusEnum锟?
     */
    private String readStatus;

    /**
     * 宸茶鏃堕棿
     */
    private LocalDateTime readAt;
}
