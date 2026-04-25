package com.manage.managesystem.mapper;

import com.manage.managesystem.entity.MilestoneEntity;
import com.manage.managesystem.vo.MilestoneVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MilestoneMapper {
    List<MilestoneVO> selectByProjectId(@Param("projectId") Long projectId);

    MilestoneVO selectById(@Param("projectId") Long projectId, @Param("id") Long id);

    MilestoneEntity selectEntityById(@Param("projectId") Long projectId, @Param("id") Long id);

    int insert(MilestoneEntity entity);

    int update(MilestoneEntity entity);

    int softDelete(@Param("projectId") Long projectId,
                   @Param("id") Long id,
                   @Param("updatedBy") Long updatedBy,
                   @Param("updatedAt") LocalDateTime updatedAt);
}
