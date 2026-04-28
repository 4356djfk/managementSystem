package com.manage.managesystem.mapper;

import com.manage.managesystem.entity.ScopeBaselineEntity;
import com.manage.managesystem.vo.ScopeBaselineVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScopeBaselineMapper {
    List<ScopeBaselineVO> selectByProjectId(@Param("projectId") Long projectId);

    ScopeBaselineEntity selectEntityById(@Param("id") Long id);

    int countByProjectId(@Param("projectId") Long projectId);

    Integer selectMaxVersionNumber(@Param("projectId") Long projectId);

    int insert(ScopeBaselineEntity entity);

    int deleteById(@Param("id") Long id);
}
