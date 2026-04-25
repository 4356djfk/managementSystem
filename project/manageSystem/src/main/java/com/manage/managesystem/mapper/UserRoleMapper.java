package com.manage.managesystem.mapper;

import com.manage.managesystem.entity.UserRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRoleMapper {
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    int insert(UserRoleEntity userRole);

    int deleteByUserId(@Param("userId") Long userId);
}
