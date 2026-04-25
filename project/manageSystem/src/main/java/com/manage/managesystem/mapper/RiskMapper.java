package com.manage.managesystem.mapper;

import com.manage.managesystem.dto.RiskQueryDto;
import com.manage.managesystem.entity.RiskEntity;
import com.manage.managesystem.vo.RiskMatrixPointVO;
import com.manage.managesystem.vo.RiskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RiskMapper {
    List<RiskVO> selectByProjectId(@Param("projectId") Long projectId,
                                   @Param("query") RiskQueryDto query);

    RiskVO selectById(@Param("id") Long id);

    RiskEntity selectEntityById(@Param("id") Long id);

    int insert(RiskEntity entity);

    int update(RiskEntity entity);

    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("closedAt") LocalDateTime closedAt,
                     @Param("updatedBy") Long updatedBy,
                     @Param("updatedAt") LocalDateTime updatedAt);

    int softDelete(@Param("id") Long id,
                   @Param("projectId") Long projectId,
                   @Param("updatedBy") Long updatedBy,
                   @Param("updatedAt") LocalDateTime updatedAt);

    List<RiskMatrixPointVO> selectRiskMatrix(@Param("projectId") Long projectId);
}
