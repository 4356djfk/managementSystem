package com.manage.managesystem.service;

import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.TaskQueryDto;
import com.manage.managesystem.mapper.ProjectMapper;
import com.manage.managesystem.mapper.TaskMapper;
import com.manage.managesystem.vo.CommentVO;
import com.manage.managesystem.vo.GanttTaskVO;
import com.manage.managesystem.vo.ScheduleAlertVO;
import com.manage.managesystem.vo.TaskDependencyVO;
import com.manage.managesystem.vo.TaskDetailVO;
import com.manage.managesystem.vo.TaskListItemVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskQueryService {
    private final TaskMapper taskMapper;
    private final ProjectMapper projectMapper;

    public TaskQueryService(TaskMapper taskMapper, ProjectMapper projectMapper) {
        this.taskMapper = taskMapper;
        this.projectMapper = projectMapper;
    }

    public PageResult<TaskListItemVO> list(Long projectId, TaskQueryDto queryDto) {
        ensureProjectExists(projectId);
        List<TaskListItemVO> list = taskMapper.selectByProjectId(projectId, queryDto);
        PageResult<TaskListItemVO> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setPage(queryDto.getPage() == null ? 1 : queryDto.getPage());
        pageResult.setPageSize(queryDto.getPageSize() == null ? list.size() : queryDto.getPageSize());
        pageResult.setTotal((long) list.size());
        return pageResult;
    }

    public TaskDetailVO detail(Long projectId, Long taskId) {
        ensureProjectExists(projectId);
        TaskDetailVO detail = taskMapper.selectDetailById(projectId, taskId);
        if (detail == null) {
            throw new IllegalArgumentException("task not found");
        }
        List<TaskDependencyVO> dependencies = taskMapper.selectDependenciesByTaskId(projectId, taskId);
        detail.setDependencies(dependencies == null ? new ArrayList<>() : dependencies);
        List<CommentVO> comments = taskMapper.selectCommentsByTaskId(projectId, taskId);
        detail.setComments(comments == null ? new ArrayList<>() : comments);
        return detail;
    }

    public List<GanttTaskVO> gantt(Long projectId) {
        ensureProjectExists(projectId);
        return taskMapper.selectGanttByProjectId(projectId);
    }

    public List<TaskListItemVO> criticalPath(Long projectId) {
        ensureProjectExists(projectId);
        return taskMapper.selectCriticalPath(projectId);
    }

    public List<ScheduleAlertVO> scheduleAlerts(Long projectId) {
        ensureProjectExists(projectId);
        return taskMapper.selectScheduleAlerts(projectId, LocalDateTime.now());
    }

    private void ensureProjectExists(Long projectId) {
        if (projectMapper.selectEntityById(projectId) == null) {
            throw new IllegalArgumentException("project not found");
        }
    }
}
