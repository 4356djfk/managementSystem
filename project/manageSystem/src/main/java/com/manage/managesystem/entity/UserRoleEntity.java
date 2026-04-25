package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 鐢ㄦ埛瑙掕壊鍏宠仈瀹炰綋
 * 瀵瑰簲琛細sys_user_role
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserRoleEntity extends BaseEntity {
    /**
     * 鐢ㄦ埛 ID
     */
    private Long userId;

    /**
     * 瑙掕壊 ID
     */
    private Long roleId;
}
