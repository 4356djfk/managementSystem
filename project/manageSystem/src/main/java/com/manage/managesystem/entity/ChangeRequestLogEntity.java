package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 鍙樻洿璇锋眰鏃ュ織瀹炰綋
 * 瀵瑰簲琛細change_request_log
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChangeRequestLogEntity extends BaseEntity {
    /**
     * 鍙樻洿璇锋眰 ID
     */
    private Long changeRequestId;

    /**
     * 鎿嶄綔
     */
    private String action;

    /**
     * 鍘熺姸锟?
     */
    private String fromStatus;

    /**
     * 鏂扮姸锟?
     */
    private String toStatus;

    /**
     * 鎿嶄綔锟?
     */
    private Long operatorId;

    /**
     * 澶囨敞
     */
    private String comment;
}
