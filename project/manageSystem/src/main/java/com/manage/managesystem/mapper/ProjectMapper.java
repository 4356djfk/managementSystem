package com.manage.managesystem.mapper;

import com.manage.managesystem.dto.ProjectQueryDto;
import com.manage.managesystem.entity.ProjectCharterEntity;
import com.manage.managesystem.entity.ProjectEntity;
import com.manage.managesystem.vo.MilestoneVO;
import com.manage.managesystem.vo.ProjectCharterVO;
import com.manage.managesystem.vo.ProjectDashboardVO;
import com.manage.managesystem.vo.ProjectDetailVO;
import com.manage.managesystem.vo.ProjectListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ProjectMapper {
    List<ProjectListItemVO> selectList(@Param("query") ProjectQueryDto query);

    ProjectDetailVO selectDetailById(@Param("projectId") Long projectId);

    ProjectEntity selectEntityById(@Param("projectId") Long projectId);

    int insert(ProjectEntity entity);

    int update(ProjectEntity entity);

    int updateStatus(@Param("projectId") Long projectId,
                     @Param("status") String status,
                     @Param("actualStartDate") LocalDate actualStartDate,
                     @Param("actualEndDate") LocalDate actualEndDate,
                     @Param("clearActualEndDate") boolean clearActualEndDate,
                     @Param("updatedBy") Long updatedBy,
                     @Param("updatedAt") LocalDateTime updatedAt);

    int softDelete(@Param("projectId") Long projectId,
                   @Param("updatedBy") Long updatedBy,
                   @Param("updatedAt") LocalDateTime updatedAt);

    ProjectCharterVO selectCharterByProjectId(@Param("projectId") Long projectId);

    int insertCharter(ProjectCharterEntity entity);

    int updateCharter(ProjectCharterEntity entity);

    ProjectDashboardVO selectDashboard(@Param("projectId") Long projectId);

    List<MilestoneVO> selectUpcomingMilestones(@Param("projectId") Long projectId);
}
