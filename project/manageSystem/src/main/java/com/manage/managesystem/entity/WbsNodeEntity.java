package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * WBS鑺傜偣瀹炰綋
 * 瀵瑰簲琛細wbs_node
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WbsNodeEntity extends BaseEntity {
    /**
     * 椤圭洰 ID
     */
    private Long projectId;

    /**
     * 鐖惰妭锟?ID
     */
    private Long parentId;

    /**
     * WBS 缂栫爜
     */
    private String nodeCode;

    /**
     * 鑺傜偣鍚嶇О
     */
    private String nodeName;

    /**
     * 鎻忚堪
     */
    private String description;

    /**
     * 璐熻矗锟?
     */
    private Long ownerId;

    /**
     * 鎺掑簭
     */
    private Integer sortOrder;

    /**
     * 灞傜骇
     */
    private Integer levelNo;
}
