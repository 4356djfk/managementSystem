package com.manage.managesystem.mapper;

import com.manage.managesystem.dto.TaskQueryDto;
import com.manage.managesystem.entity.CommentEntity;
import com.manage.managesystem.entity.TaskEntity;
import com.manage.managesystem.entity.TaskDependencyEntity;
import com.manage.managesystem.vo.CommentVO;
import com.manage.managesystem.vo.GanttTaskVO;
import com.manage.managesystem.vo.ScheduleAlertVO;
import com.manage.managesystem.vo.TaskDependencyVO;
import com.manage.managesystem.vo.TaskDetailVO;
import com.manage.managesystem.vo.TaskListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TaskMapper {
    List<TaskListItemVO> selectByProjectId(@Param("projectId") Long projectId,
                                           @Param("query") TaskQueryDto query);

    TaskDetailVO selectDetailById(@Param("projectId") Long projectId,
                                  @Param("taskId") Long taskId);

    List<TaskDependencyVO> selectDependenciesByTaskId(@Param("projectId") Long projectId,
                                                      @Param("taskId") Long taskId);

    List<TaskDependencyVO> selectDependenciesByProjectId(@Param("projectId") Long projectId);

    TaskEntity selectEntityById(@Param("projectId") Long projectId,
                                @Param("taskId") Long taskId);

    List<TaskEntity> selectEntitiesByProjectId(@Param("projectId") Long projectId);

    List<CommentVO> selectCommentsByTaskId(@Param("projectId") Long projectId,
                                           @Param("taskId") Long taskId);

    CommentVO selectCommentById(@Param("projectId") Long projectId,
                                @Param("id") Long id);

    CommentEntity selectCommentEntityById(@Param("projectId") Long projectId,
                                          @Param("id") Long id);

    int insert(TaskEntity entity);

    int insertComment(CommentEntity entity);

    int update(TaskEntity entity);

    int softDelete(@Param("projectId") Long projectId,
                   @Param("taskId") Long taskId,
                   @Param("updatedBy") Long updatedBy,
                   @Param("updatedAt") LocalDateTime updatedAt);

    int updateProgress(@Param("projectId") Long projectId,
                       @Param("taskId") Long taskId,
                       @Param("status") String status,
                       @Param("progress") BigDecimal progress,
                       @Param("assigneeId") Long assigneeId,
                       @Param("remark") String remark,
                       @Param("actualStartDate") LocalDateTime actualStartDate,
                       @Param("actualEndDate") LocalDateTime actualEndDate,
                       @Param("updatedBy") Long updatedBy,
                       @Param("updatedAt") LocalDateTime updatedAt);

    int updateDerivedProgress(@Param("projectId") Long projectId,
                              @Param("taskId") Long taskId,
                              @Param("status") String status,
                              @Param("progress") BigDecimal progress,
                              @Param("updatedBy") Long updatedBy,
                              @Param("updatedAt") LocalDateTime updatedAt);

    int updateActualMetrics(@Param("projectId") Long projectId,
                            @Param("taskId") Long taskId,
                            @Param("actualHours") BigDecimal actualHours,
                            @Param("actualCost") BigDecimal actualCost,
                            @Param("updatedBy") Long updatedBy,
                            @Param("updatedAt") LocalDateTime updatedAt);

    List<GanttTaskVO> selectGanttByProjectId(@Param("projectId") Long projectId);

    List<TaskListItemVO> selectCriticalPath(@Param("projectId") Long projectId);

    List<ScheduleAlertVO> selectScheduleAlerts(@Param("projectId") Long projectId,
                                               @Param("now") LocalDateTime now);

    TaskDependencyVO selectDependencyById(@Param("projectId") Long projectId,
                                          @Param("id") Long id);

    int insertDependency(TaskDependencyEntity entity);

    int deleteDependency(@Param("projectId") Long projectId,
                         @Param("id") Long id);

    int softDeleteComment(@Param("projectId") Long projectId,
                          @Param("id") Long id,
                          @Param("updatedAt") LocalDateTime updatedAt);
}
