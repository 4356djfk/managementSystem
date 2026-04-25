package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 椤圭洰妯℃澘瀹炰綋
 * 瀵瑰簲琛細project_template
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectTemplateEntity extends BaseEntity {
    /**
     * 妯℃澘鍚嶇О
     */
    private String name;

    /**
     * 妯℃澘绫诲瀷
     */
    private String type;

    /**
     * 鎻忚堪
     */
    private String description;

    /**
     * 鏄惁绯荤粺妯℃澘
     */
    private Integer isSystem;

    /**
     * 妯℃澘鐘舵€侊紙鍙傦拷?ProjectTemplateStatusEnum锟?
     */
    private String status;
}
