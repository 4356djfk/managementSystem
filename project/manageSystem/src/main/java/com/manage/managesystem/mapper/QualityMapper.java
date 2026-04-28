package com.manage.managesystem.mapper;

import com.manage.managesystem.dto.QualityActivityQueryDto;
import com.manage.managesystem.dto.QualityMetricQueryDto;
import com.manage.managesystem.dto.QualityPlanQueryDto;
import com.manage.managesystem.entity.QualityActivityEntity;
import com.manage.managesystem.entity.QualityMetricEntity;
import com.manage.managesystem.entity.QualityPlanEntity;
import com.manage.managesystem.vo.QualityActivityVO;
import com.manage.managesystem.vo.QualityMetricVO;
import com.manage.managesystem.vo.QualityPlanVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QualityMapper {
    List<QualityPlanVO> selectPlansByProjectId(@Param("projectId") Long projectId,
                                               @Param("query") QualityPlanQueryDto query);

    QualityPlanVO selectPlanById(@Param("projectId") Long projectId, @Param("id") Long id);

    QualityPlanEntity selectPlanEntityById(@Param("projectId") Long projectId, @Param("id") Long id);

    int insertPlan(QualityPlanEntity entity);

    int updatePlan(QualityPlanEntity entity);

    int softDeletePlan(@Param("projectId") Long projectId, @Param("id") Long id);

    int countActivitiesByPlanId(@Param("projectId") Long projectId, @Param("qualityPlanId") Long qualityPlanId);

    List<QualityActivityVO> selectActivitiesByProjectId(@Param("projectId") Long projectId,
                                                        @Param("query") QualityActivityQueryDto query);

    QualityActivityVO selectActivityById(@Param("projectId") Long projectId, @Param("id") Long id);

    QualityActivityEntity selectActivityEntityById(@Param("projectId") Long projectId, @Param("id") Long id);

    int insertActivity(QualityActivityEntity entity);

    int updateActivity(QualityActivityEntity entity);

    int softDeleteActivity(@Param("projectId") Long projectId, @Param("id") Long id);

    List<QualityMetricVO> selectMetricsByProjectId(@Param("projectId") Long projectId,
                                                   @Param("query") QualityMetricQueryDto query);

    QualityMetricVO selectMetricById(@Param("projectId") Long projectId, @Param("id") Long id);

    QualityMetricEntity selectMetricEntityById(@Param("projectId") Long projectId, @Param("id") Long id);

    int insertMetric(QualityMetricEntity entity);

    int updateMetric(QualityMetricEntity entity);

    int deleteMetric(@Param("projectId") Long projectId, @Param("id") Long id);
}
