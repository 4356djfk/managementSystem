package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 鑼冨洿鍩虹嚎瀹炰綋
 * 瀵瑰簲琛細scope_baseline
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ScopeBaselineEntity extends BaseEntity {
    /**
     * 椤圭洰 ID
     */
    private Long projectId;

    /**
     * 鐗堟湰锟?
     */
    private String versionNo;

    /**
     * 鍩虹嚎鍚嶇О
     */
    private String baselineName;

    /**
     * 璇存槑
     */
    private String description;

    /**
     * 蹇収 JSON
     */
    private String snapshotJson;

    /**
     * 鐘讹拷?
     */
    private String status;

    /**
     * 鍙戝竷锟?
     */
    private Long publishedBy;

    /**
     * 鍙戝竷鏃堕棿
     */
    private LocalDateTime publishedAt;
}
