package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 绯荤粺鐢ㄦ埛瀹炰綋
 * 瀵瑰簲琛細sys_user
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserEntity extends BaseEntity {
    /**
     * 鐧诲綍璐﹀彿
     */
    private String username;

    /**
     * 瀵嗙爜瀵嗘枃
     */
    private String passwordHash;

    /**
     * 鐪熷疄濮撳悕
     */
    private String realName;

    /**
     * 閭
     */
    private String email;

    /**
     * 鎵嬫満锟?
     */
    private String phone;

    /**
     * 澶村儚鍦板潃
     */
    private String avatarUrl;

    /**
     * 鐢ㄦ埛鐘舵€侊紙鍙傦拷?UserStatusEnum锟?
     */
    private String status;

    /**
     * 鏈€鍚庣櫥褰曟椂锟?
     */
    private LocalDateTime lastLoginAt;
}
