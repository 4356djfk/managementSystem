package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 浠诲姟璇勮瀹炰綋
 * 瀵瑰簲琛細task_comment
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentEntity extends BaseEntity {
    /**
     * 椤圭洰 ID
     */
    private Long projectId;

    /**
     * 浠诲姟 ID
     */
    private Long taskId;

    /**
     * 璇勮锟?
     */
    private Long userId;

    /**
     * 璇勮鍐呭
     */
    private String content;

    /**
     * 鍥炲鐩爣璇勮 ID
     */
    private Long replyToId;
}
