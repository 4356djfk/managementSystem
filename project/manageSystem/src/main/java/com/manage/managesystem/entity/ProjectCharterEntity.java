package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 椤圭洰绔犵▼瀹炰綋
 * 瀵瑰簲琛細project_charter
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectCharterEntity extends BaseEntity {
    /**
     * 椤圭洰 ID
     */
    private Long projectId;

    /**
     * 椤圭洰鐩爣
     */
    private String objective;

    /**
     * 鑼冨洿姒傝堪
     */
    private String scopeSummary;

    /**
     * 鍙戣捣锟?
     */
    private String sponsor;

    /**
     * 瀹℃壒锟?
     */
    private String approver;

    /**
     * 骞茬郴浜鸿锟?
     */
    private String stakeholders;

    /**
     * 鎴愬姛鏍囧噯
     */
    private String successCriteria;
}
