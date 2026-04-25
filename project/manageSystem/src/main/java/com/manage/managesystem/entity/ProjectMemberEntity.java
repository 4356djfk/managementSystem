package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 椤圭洰鎴愬憳瀹炰綋
 * 瀵瑰簲琛細project_member
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectMemberEntity extends BaseEntity {
    /**
     * 椤圭洰 ID
     */
    private Long projectId;

    /**
     * 鐢ㄦ埛 ID
     */
    private Long userId;

    /**
     * 椤圭洰瑙掕壊锛堝弬锟?SystemRoleEnum锟?
     */
    private String projectRole;

    /**
     * 鎴愬憳鐘舵€侊紙鍙傦拷?ProjectMemberStatusEnum锟?
     */
    private String memberStatus;

    /**
     * 鍔犲叆鏃堕棿
     */
    private LocalDateTime joinedAt;

    /**
     * 绂诲紑鏃堕棿
     */
    private LocalDateTime leftAt;
}
