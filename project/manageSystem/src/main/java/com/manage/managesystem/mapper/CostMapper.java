package com.manage.managesystem.mapper;

import com.manage.managesystem.entity.CostActualEntity;
import com.manage.managesystem.entity.CostPlanEntity;
import com.manage.managesystem.vo.CostActualVO;
import com.manage.managesystem.vo.CostBaselineVO;
import com.manage.managesystem.vo.CostPlanVO;
import com.manage.managesystem.vo.EvmMetricVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CostMapper {
    List<CostPlanVO> selectPlansByProjectId(@Param("projectId") Long projectId);

    CostPlanVO selectPlanById(@Param("projectId") Long projectId, @Param("id") Long id);

    CostPlanEntity selectPlanEntityById(@Param("projectId") Long projectId, @Param("id") Long id);

    int insertPlan(CostPlanEntity entity);

    int updatePlan(CostPlanEntity entity);

    int softDeletePlan(@Param("projectId") Long projectId,
                       @Param("id") Long id,
                       @Param("updatedBy") Long updatedBy,
                       @Param("updatedAt") LocalDateTime updatedAt);

    List<CostActualVO> selectActualsByProjectId(@Param("projectId") Long projectId);

    CostActualVO selectActualById(@Param("projectId") Long projectId, @Param("id") Long id);

    CostActualEntity selectActualEntityById(@Param("projectId") Long projectId, @Param("id") Long id);

    int insertActual(CostActualEntity entity);

    int softDeleteActual(@Param("projectId") Long projectId,
                         @Param("id") Long id,
                         @Param("updatedAt") LocalDateTime updatedAt);

    java.math.BigDecimal sumPlannedLaborAmountByTask(@Param("projectId") Long projectId, @Param("taskId") Long taskId);

    int countBaselines(@Param("projectId") Long projectId, @Param("baselineType") String baselineType);

    List<CostBaselineVO> selectBaselinesByProjectId(@Param("projectId") Long projectId,
                                                    @Param("baselineType") String baselineType);

    int insertBaseline(@Param("id") Long id,
                       @Param("projectId") Long projectId,
                       @Param("baselineType") String baselineType,
                       @Param("versionNo") String versionNo,
                       @Param("baselineName") String baselineName,
                       @Param("snapshotJson") String snapshotJson,
                       @Param("status") String status,
                       @Param("publishedBy") Long publishedBy,
                       @Param("publishedAt") LocalDateTime publishedAt,
                       @Param("createdAt") LocalDateTime createdAt);

    CostBaselineVO selectBaselineById(@Param("projectId") Long projectId, @Param("id") Long id);

    int deleteBaseline(@Param("projectId") Long projectId, @Param("id") Long id);

    EvmMetricVO selectEvmMetric(@Param("projectId") Long projectId);
}
