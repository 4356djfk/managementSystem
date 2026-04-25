package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 闇€姹備俊鎭疄锟?
 * 瀵瑰簲琛細requirement_info
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RequirementEntity extends BaseEntity {
    /**
     * 椤圭洰 ID
     */
    private Long projectId;

    /**
     * WBS 鑺傜偣 ID
     */
    private Long wbsId;

    /**
     * 闇€姹傜紪锟?
     */
    private String requirementCode;

    /**
     * 鏍囬
     */
    private String title;

    /**
     * 闇€姹傜被鍨嬶紙鍙傦拷?RequirementTypeEnum锟?
     */
    private String requirementType;

    /**
     * 浼樺厛绾э紙鍙傦拷?PriorityEnum锟?
     */
    private String priority;

    /**
     * 鐘舵€侊紙鍙傦拷?RequirementStatusEnum锟?
     */
    private String status;

    /**
     * 鎻忚堪
     */
    private String description;

    /**
     * 鎻愬嚭锟?
     */
    private Long proposerId;
}
