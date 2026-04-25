package com.manage.managesystem.mapper;

import com.manage.managesystem.entity.RoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {
    RoleEntity selectByRoleCode(@Param("roleCode") String roleCode);

    List<RoleEntity> selectByRoleCodes(@Param("roleCodes") List<String> roleCodes);

    List<RoleEntity> selectRolesByUserId(@Param("userId") Long userId);

    int insert(RoleEntity role);
}
