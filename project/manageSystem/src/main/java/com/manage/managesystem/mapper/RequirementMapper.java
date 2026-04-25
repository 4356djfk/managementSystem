package com.manage.managesystem.mapper;

import com.manage.managesystem.dto.RequirementQueryDto;
import com.manage.managesystem.entity.RequirementEntity;
import com.manage.managesystem.vo.RequirementVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RequirementMapper {
    List<RequirementVO> selectByProjectId(@Param("projectId") Long projectId,
                                          @Param("query") RequirementQueryDto query);

    RequirementVO selectById(@Param("id") Long id);

    RequirementEntity selectEntityById(@Param("id") Long id);

    int insert(RequirementEntity entity);

    int update(RequirementEntity entity);
}
