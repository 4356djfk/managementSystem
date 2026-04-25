package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 浠诲姟渚濊禆瀹炰綋
 * 瀵瑰簲琛細task_dependency
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskDependencyEntity extends BaseEntity {
    /**
     * 椤圭洰 ID
     */
    private Long projectId;

    /**
     * 鍓嶇疆浠诲姟 ID
     */
    private Long predecessorTaskId;

    /**
     * 鍚庣户浠诲姟 ID
     */
    private Long successorTaskId;

    /**
     * 渚濊禆绫诲瀷锛堝弬锟?DependencyTypeEnum锟?
     */
    private String dependencyType;
}
