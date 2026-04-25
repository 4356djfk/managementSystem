package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 绯荤粺瑙掕壊瀹炰綋
 * 瀵瑰簲琛細sys_role
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleEntity extends BaseEntity {
    /**
     * 瑙掕壊缂栫爜
     */
    private String roleCode;

    /**
     * 瑙掕壊鍚嶇О
     */
    private String roleName;

    /**
     * 瑙掕壊鎻忚堪
     */
    private String description;
}
