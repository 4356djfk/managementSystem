package com.manage.managesystem.mapper;

import com.manage.managesystem.dto.DefectQueryDto;
import com.manage.managesystem.dto.TestCaseQueryDto;
import com.manage.managesystem.dto.TestPlanQueryDto;
import com.manage.managesystem.entity.DefectEntity;
import com.manage.managesystem.entity.TestCaseEntity;
import com.manage.managesystem.entity.TestPlanEntity;
import com.manage.managesystem.vo.DefectVO;
import com.manage.managesystem.vo.TestCaseVO;
import com.manage.managesystem.vo.TestPlanVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TestManagementMapper {
    List<TestPlanVO> selectPlansByProjectId(@Param("projectId") Long projectId,
                                            @Param("query") TestPlanQueryDto query);

    TestPlanVO selectPlanById(@Param("projectId") Long projectId, @Param("id") Long id);

    TestPlanEntity selectPlanEntityById(@Param("projectId") Long projectId, @Param("id") Long id);

    int insertPlan(TestPlanEntity entity);

    int updatePlan(TestPlanEntity entity);

    int softDeletePlan(@Param("projectId") Long projectId,
                       @Param("id") Long id,
                       @Param("updatedAt") LocalDateTime updatedAt);

    int countCasesByPlanId(@Param("projectId") Long projectId, @Param("testPlanId") Long testPlanId);

    List<TestCaseVO> selectCasesByProjectId(@Param("projectId") Long projectId,
                                            @Param("query") TestCaseQueryDto query);

    TestCaseVO selectCaseById(@Param("projectId") Long projectId, @Param("id") Long id);

    TestCaseEntity selectCaseEntityById(@Param("projectId") Long projectId, @Param("id") Long id);

    int insertCase(TestCaseEntity entity);

    int updateCase(TestCaseEntity entity);

    int softDeleteCase(@Param("projectId") Long projectId,
                       @Param("id") Long id,
                       @Param("updatedAt") LocalDateTime updatedAt);

    int countDefectsByCaseId(@Param("projectId") Long projectId, @Param("testCaseId") Long testCaseId);

    List<DefectVO> selectDefectsByProjectId(@Param("projectId") Long projectId,
                                            @Param("query") DefectQueryDto query);

    DefectVO selectDefectById(@Param("projectId") Long projectId, @Param("id") Long id);

    DefectEntity selectDefectEntityById(@Param("projectId") Long projectId, @Param("id") Long id);

    int insertDefect(DefectEntity entity);

    int updateDefect(DefectEntity entity);

    int softDeleteDefect(@Param("projectId") Long projectId,
                         @Param("id") Long id,
                         @Param("updatedAt") LocalDateTime updatedAt);
}
