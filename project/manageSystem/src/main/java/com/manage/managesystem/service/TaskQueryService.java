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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TaskQueryService {
    private final TaskMapper taskMapper;
    private final ProjectMapper projectMapper;
    private final ProjectPermissionService projectPermissionService;
    private final TaskProgressRollupService taskProgressRollupService;

    public TaskQueryService(TaskMapper taskMapper,
                            ProjectMapper projectMapper,
                            ProjectPermissionService projectPermissionService,
                            TaskProgressRollupService taskProgressRollupService) {
        this.taskMapper = taskMapper;
        this.projectMapper = projectMapper;
        this.projectPermissionService = projectPermissionService;
        this.taskProgressRollupService = taskProgressRollupService;
    }

    public PageResult<TaskListItemVO> list(Long projectId, TaskQueryDto queryDto) {
        projectPermissionService.ensureProjectParticipant(projectId);
        TaskQueryDto effectiveQuery = queryDto == null ? new TaskQueryDto() : queryDto;
        List<TaskListItemVO> list = loadTaskList(projectId, effectiveQuery);
        applyEffectiveAssignees(projectId, list);
        applyTaskRollups(projectId, list);
        PageResult<TaskListItemVO> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setPage(effectiveQuery.getPage() == null ? 1 : effectiveQuery.getPage());
        pageResult.setPageSize(effectiveQuery.getPageSize() == null ? list.size() : effectiveQuery.getPageSize());
        pageResult.setTotal((long) list.size());
        return pageResult;
    }

    public TaskDetailVO detail(Long projectId, Long taskId) {
        projectPermissionService.ensureProjectParticipant(projectId);
        TaskDetailVO detail = taskMapper.selectDetailById(projectId, taskId);
        if (detail == null) {
            throw new IllegalArgumentException("task not found");
        }
        applyEffectiveAssignees(projectId, List.of(detail));
        applyTaskRollups(projectId, List.of(detail));
        List<TaskDependencyVO> dependencies = taskMapper.selectDependenciesByTaskId(projectId, taskId);
        detail.setDependencies(dependencies == null ? new ArrayList<>() : dependencies);
        List<CommentVO> comments = taskMapper.selectCommentsByTaskId(projectId, taskId);
        detail.setComments(comments == null ? new ArrayList<>() : comments);
        return detail;
    }

    public List<CommentVO> listComments(Long projectId, Long taskId) {
        projectPermissionService.ensureProjectParticipant(projectId);
        TaskDetailVO detail = taskMapper.selectDetailById(projectId, taskId);
        if (detail == null) {
            throw new IllegalArgumentException("task not found");
        }
        List<CommentVO> comments = taskMapper.selectCommentsByTaskId(projectId, taskId);
        return comments == null ? new ArrayList<>() : comments;
    }

    public List<GanttTaskVO> gantt(Long projectId) {
        projectPermissionService.ensureProjectParticipant(projectId);
        List<GanttTaskVO> tasks = taskMapper.selectGanttByProjectId(projectId);
        Map<Long, TaskProgressRollupService.RollupSnapshot> rollups = taskProgressRollupService.calculateRollups(projectId);
        tasks.forEach(item -> {
            TaskProgressRollupService.RollupSnapshot snapshot = rollups.get(item.getId());
            if (snapshot != null) {
                item.setProgress(snapshot.progress());
            }
        });
        return tasks;
    }

    public List<TaskListItemVO> criticalPath(Long projectId) {
        projectPermissionService.ensureProjectParticipant(projectId);
        List<TaskListItemVO> list = taskMapper.selectCriticalPath(projectId);
        applyEffectiveAssignees(projectId, list);
        applyTaskRollups(projectId, list);
        return list;
    }

    public List<ScheduleAlertVO> scheduleAlerts(Long projectId) {
        projectPermissionService.ensureProjectParticipant(projectId);
        return taskMapper.selectScheduleAlerts(projectId, LocalDateTime.now());
    }

    private List<TaskListItemVO> loadTaskList(Long projectId, TaskQueryDto queryDto) {
        if (queryDto == null || queryDto.getAssigneeId() == null) {
            return taskMapper.selectByProjectId(projectId, queryDto);
        }
        List<TaskListItemVO> allMatchingTasks = taskMapper.selectByProjectId(projectId, copyTaskQueryWithoutAssignee(queryDto));
        if (allMatchingTasks.isEmpty()) {
            return allMatchingTasks;
        }
        applyEffectiveAssignees(projectId, allMatchingTasks);
        return allMatchingTasks.stream()
                .filter(item -> Objects.equals(item.getAssigneeId(), queryDto.getAssigneeId()))
                .toList();
    }

    private TaskQueryDto copyTaskQueryWithoutAssignee(TaskQueryDto queryDto) {
        TaskQueryDto copy = new TaskQueryDto();
        copy.setStatus(queryDto.getStatus());
        copy.setMilestoneId(queryDto.getMilestoneId());
        copy.setKeyword(queryDto.getKeyword());
        copy.setPage(queryDto.getPage());
        copy.setPageSize(queryDto.getPageSize());
        return copy;
    }

    private void applyEffectiveAssignees(Long projectId, List<? extends TaskListItemVO> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        List<TaskListItemVO> allProjectTasks = taskMapper.selectByProjectId(projectId, null);
        if (allProjectTasks == null || allProjectTasks.isEmpty()) {
            return;
        }
        Map<Long, TaskListItemVO> taskMap = new HashMap<>();
        allProjectTasks.forEach(task -> {
            if (task.getId() != null) {
                taskMap.put(task.getId(), task);
            }
        });
        Map<Long, TaskAssigneeSnapshot> memo = new HashMap<>();
        tasks.forEach(task -> {
            TaskAssigneeSnapshot snapshot = resolveEffectiveAssignee(task, taskMap, memo, new HashSet<>());
            if (snapshot == null) {
                return;
            }
            task.setAssigneeId(snapshot.assigneeId);
            task.setAssigneeName(snapshot.assigneeName);
        });
    }

    private TaskAssigneeSnapshot resolveEffectiveAssignee(TaskListItemVO task,
                                                          Map<Long, TaskListItemVO> taskMap,
                                                          Map<Long, TaskAssigneeSnapshot> memo,
                                                          HashSet<Long> visiting) {
        if (task == null || task.getId() == null) {
            return null;
        }
        TaskAssigneeSnapshot cached = memo.get(task.getId());
        if (cached != null) {
            return cached;
        }
        if (!visiting.add(task.getId())) {
            TaskAssigneeSnapshot fallback = directAssigneeSnapshot(task);
            memo.put(task.getId(), fallback);
            return fallback;
        }
        TaskAssigneeSnapshot snapshot;
        if (task.getDirectAssigneeId() != null) {
            snapshot = directAssigneeSnapshot(task);
        } else if (task.getParentTaskId() != null) {
            TaskListItemVO parent = taskMap.get(task.getParentTaskId());
            TaskAssigneeSnapshot parentSnapshot = resolveEffectiveAssignee(parent, taskMap, memo, visiting);
            snapshot = parentSnapshot != null
                    ? parentSnapshot
                    : directAssigneeSnapshot(task);
        } else {
            snapshot = directAssigneeSnapshot(task);
        }
        visiting.remove(task.getId());
        memo.put(task.getId(), snapshot);
        return snapshot;
    }

    private TaskAssigneeSnapshot directAssigneeSnapshot(TaskListItemVO task) {
        return new TaskAssigneeSnapshot(task.getDirectAssigneeId(), task.getDirectAssigneeName());
    }

    private void ensureProjectExists(Long projectId) {
        if (projectMapper.selectEntityById(projectId) == null) {
            throw new IllegalArgumentException("project not found");
        }
    }

    private void applyTaskRollups(Long projectId, List<? extends TaskListItemVO> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        Map<Long, TaskProgressRollupService.RollupSnapshot> rollups = taskProgressRollupService.calculateRollups(projectId);
        tasks.forEach(item -> {
            TaskProgressRollupService.RollupSnapshot snapshot = rollups.get(item.getId());
            if (snapshot == null) {
                return;
            }
            item.setStatus(snapshot.status());
            item.setProgress(snapshot.progress());
        });
    }

    private static class TaskAssigneeSnapshot {
        private final Long assigneeId;
        private final String assigneeName;

        private TaskAssigneeSnapshot(Long assigneeId, String assigneeName) {
            this.assigneeId = assigneeId;
            this.assigneeName = assigneeName;
        }
    }
}
