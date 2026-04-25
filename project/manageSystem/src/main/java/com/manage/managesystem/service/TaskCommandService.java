package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.dto.CreateTaskDependencyDto;
import com.manage.managesystem.dto.CreateTaskCommentDto;
import com.manage.managesystem.dto.CreateTaskDto;
import com.manage.managesystem.dto.UpdateTaskDto;
import com.manage.managesystem.dto.UpdateTaskProgressDto;
import com.manage.managesystem.entity.CommentEntity;
import com.manage.managesystem.entity.TaskDependencyEntity;
import com.manage.managesystem.entity.TaskEntity;
import com.manage.managesystem.entity.ProjectEntity;
import com.manage.managesystem.enums.DependencyTypeEnum;
import com.manage.managesystem.enums.PriorityEnum;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.enums.TaskStatusEnum;
import com.manage.managesystem.enums.TaskTypeEnum;
import com.manage.managesystem.mapper.ProjectMemberMapper;
import com.manage.managesystem.mapper.ProjectMapper;
import com.manage.managesystem.mapper.TaskMapper;
import com.manage.managesystem.mapper.UserMapper;
import com.manage.managesystem.vo.TaskDependencyVO;
import com.manage.managesystem.vo.TaskDetailVO;
import com.manage.managesystem.vo.CommentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskCommandService {
    private final TaskMapper taskMapper;
    private final TaskQueryService taskQueryService;
    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final UserMapper userMapper;

    public TaskCommandService(TaskMapper taskMapper,
                              TaskQueryService taskQueryService,
                              ProjectMapper projectMapper,
                              ProjectMemberMapper projectMemberMapper,
                              UserMapper userMapper) {
        this.taskMapper = taskMapper;
        this.taskQueryService = taskQueryService;
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public TaskDetailVO create(Long projectId, CreateTaskDto dto) {
        ensureProjectExists(projectId);
        validateTaskRelations(projectId, dto.getParentTaskId(), dto.getAssigneeId());
        LocalDateTime now = LocalDateTime.now();

        TaskEntity entity = new TaskEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setParentTaskId(dto.getParentTaskId());
        entity.setWbsId(dto.getWbsId());
        entity.setMilestoneId(dto.getMilestoneId());
        entity.setTaskCode("TSK" + entity.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setTaskType(parseTaskType(dto.getTaskType()).name());
        entity.setPriority(parsePriority(dto.getPriority()).name());
        String taskStatus = dto.getStatus() == null || dto.getStatus().isBlank()
                ? TaskStatusEnum.TODO.name()
                : parseStatus(dto.getStatus()).name();
        entity.setStatus(taskStatus);
        entity.setProgress(normalizeProgress(dto.getProgress(), taskStatus));
        entity.setAssigneeId(dto.getAssigneeId());
        entity.setPlannedStartDate(dto.getPlannedStartDate());
        entity.setPlannedEndDate(dto.getPlannedEndDate());
        entity.setPlannedHours(dto.getPlannedHours() == null ? BigDecimal.ZERO : dto.getPlannedHours());
        entity.setActualHours(BigDecimal.ZERO);
        entity.setPlannedCost(BigDecimal.ZERO);
        entity.setActualCost(BigDecimal.ZERO);
        entity.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        entity.setRemark(dto.getRemark());
        entity.setCreatedBy(UserContextHolder.getUserId());
        entity.setCreatedAt(now);
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        taskMapper.insert(entity);
        return taskQueryService.detail(projectId, entity.getId());
    }

    @Transactional
    public TaskDetailVO update(Long projectId, Long taskId, UpdateTaskDto dto) {
        ensureProjectExists(projectId);
        TaskEntity entity = requireTask(projectId, taskId);
        validateTaskRelations(projectId, dto.getParentTaskId(), dto.getAssigneeId());
        entity.setParentTaskId(dto.getParentTaskId());
        entity.setWbsId(dto.getWbsId());
        entity.setMilestoneId(dto.getMilestoneId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setTaskType(parseTaskType(dto.getTaskType()).name());
        entity.setPriority(parsePriority(dto.getPriority()).name());
        entity.setStatus(dto.getStatus() == null || dto.getStatus().isBlank() ? entity.getStatus() : parseStatus(dto.getStatus()).name());
        entity.setProgress(normalizeProgress(dto.getProgress(), entity.getStatus()));
        entity.setAssigneeId(dto.getAssigneeId());
        entity.setPlannedStartDate(dto.getPlannedStartDate());
        entity.setPlannedEndDate(dto.getPlannedEndDate());
        entity.setPlannedHours(dto.getPlannedHours() == null ? BigDecimal.ZERO : dto.getPlannedHours());
        entity.setSortOrder(dto.getSortOrder() == null ? entity.getSortOrder() : dto.getSortOrder());
        entity.setRemark(dto.getRemark());
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(LocalDateTime.now());
        taskMapper.update(entity);
        return taskQueryService.detail(projectId, taskId);
    }

    @Transactional
    public void delete(Long projectId, Long taskId) {
        ensureProjectExists(projectId);
        requireTask(projectId, taskId);
        taskMapper.softDelete(projectId, taskId, UserContextHolder.getUserId(), LocalDateTime.now());
    }

    @Transactional
    public TaskDetailVO updateProgress(Long projectId, Long taskId, UpdateTaskProgressDto dto) {
        ensureProjectExists(projectId);
        TaskEntity entity = requireTask(projectId, taskId);
        ensureProgressPermission(entity);
        BigDecimal progress = normalizeProgress(dto.getProgress(), dto.getStatus());
        TaskStatusEnum status = parseStatus(dto.getStatus());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime actualStartDate = status == TaskStatusEnum.IN_PROGRESS ? now : null;
        LocalDateTime actualEndDate = status == TaskStatusEnum.DONE ? now : null;
        taskMapper.updateProgress(projectId, taskId, status.name(), progress, dto.getRemark(),
                actualStartDate, actualEndDate, UserContextHolder.getUserId(), now);
        return taskQueryService.detail(projectId, taskId);
    }

    public List<TaskDependencyVO> listDependencies(Long projectId) {
        ensureProjectExists(projectId);
        return taskMapper.selectDependenciesByProjectId(projectId);
    }

    @Transactional
    public TaskDependencyVO createDependency(Long projectId, CreateTaskDependencyDto dto) {
        ensureProjectExists(projectId);
        if (dto.getPredecessorTaskId().equals(dto.getSuccessorTaskId())) {
            throw new IllegalArgumentException("predecessor and successor cannot be the same task");
        }
        requireTask(projectId, dto.getPredecessorTaskId());
        requireTask(projectId, dto.getSuccessorTaskId());
        validateNoSimpleCycle(projectId, dto.getPredecessorTaskId(), dto.getSuccessorTaskId());

        TaskDependencyEntity entity = new TaskDependencyEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setPredecessorTaskId(dto.getPredecessorTaskId());
        entity.setSuccessorTaskId(dto.getSuccessorTaskId());
        entity.setDependencyType(parseDependencyType(dto.getDependencyType()).name());
        entity.setCreatedAt(LocalDateTime.now());
        taskMapper.insertDependency(entity);
        return requireDependency(projectId, entity.getId());
    }

    @Transactional
    public void deleteDependency(Long projectId, Long id) {
        ensureProjectExists(projectId);
        requireDependency(projectId, id);
        taskMapper.deleteDependency(projectId, id);
    }

    @Transactional
    public CommentVO createComment(Long projectId, Long taskId, CreateTaskCommentDto dto) {
        ensureProjectExists(projectId);
        requireTask(projectId, taskId);
        if (dto.getReplyToId() != null) {
            requireComment(projectId, dto.getReplyToId());
        }
        LocalDateTime now = LocalDateTime.now();
        CommentEntity entity = new CommentEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setTaskId(taskId);
        entity.setUserId(UserContextHolder.getUserId());
        entity.setContent(dto.getContent());
        entity.setReplyToId(dto.getReplyToId());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        taskMapper.insertComment(entity);
        return requireComment(projectId, entity.getId());
    }

    @Transactional
    public void deleteComment(Long projectId, Long taskId, Long id) {
        ensureProjectExists(projectId);
        requireTask(projectId, taskId);
        requireComment(projectId, id);
        taskMapper.softDeleteComment(projectId, id, LocalDateTime.now());
    }

    private void ensureProjectExists(Long projectId) {
        if (projectMapper.selectEntityById(projectId) == null) {
            throw new IllegalArgumentException("project not found");
        }
    }

    private void validateTaskRelations(Long projectId, Long parentTaskId, Long assigneeId) {
        if (parentTaskId != null) {
            requireTask(projectId, parentTaskId);
        }
        if (assigneeId != null && userMapper.selectById(assigneeId) == null) {
            throw new IllegalArgumentException("task assignee not found: " + assigneeId);
        }
        if (assigneeId != null && !isAssignableProjectUser(projectId, assigneeId)) {
            throw new IllegalArgumentException("task assignee must be an active project member or project owner");
        }
    }

    private boolean isAssignableProjectUser(Long projectId, Long userId) {
        ProjectEntity project = projectMapper.selectEntityById(projectId);
        if (project != null && userId.equals(project.getOwnerId())) {
            return true;
        }
        return projectMemberMapper.countActiveMemberByProjectAndUser(projectId, userId) > 0;
    }

    private void ensureProgressPermission(TaskEntity entity) {
        var authUser = UserContextHolder.get();
        if (authUser == null) {
            throw new IllegalArgumentException("user not authenticated");
        }
        var roleCodes = authUser.getRoleCodes();
        boolean isPrivileged = roleCodes != null && (
                roleCodes.contains(SystemRoleEnum.SYS_ADMIN.name())
                        || roleCodes.contains(SystemRoleEnum.PROJECT_MANAGER.name())
        );
        if (isPrivileged) {
            return;
        }
        if (roleCodes != null && roleCodes.contains(SystemRoleEnum.TEAM_MEMBER.name())
                && authUser.getUserId() != null
                && authUser.getUserId().equals(entity.getAssigneeId())) {
            return;
        }
        throw new IllegalArgumentException("team members can only update progress for their assigned tasks");
    }

    private TaskEntity requireTask(Long projectId, Long taskId) {
        TaskEntity entity = taskMapper.selectEntityById(projectId, taskId);
        if (entity == null) {
            throw new IllegalArgumentException("task not found");
        }
        return entity;
    }

    private TaskDependencyVO requireDependency(Long projectId, Long id) {
        TaskDependencyVO dependency = taskMapper.selectDependencyById(projectId, id);
        if (dependency == null) {
            throw new IllegalArgumentException("task dependency not found");
        }
        return dependency;
    }

    private CommentVO requireComment(Long projectId, Long id) {
        CommentVO comment = taskMapper.selectCommentById(projectId, id);
        if (comment == null) {
            throw new IllegalArgumentException("task comment not found");
        }
        return comment;
    }

    private void validateNoSimpleCycle(Long projectId, Long predecessorTaskId, Long successorTaskId) {
        List<TaskDependencyVO> existing = taskMapper.selectDependenciesByProjectId(projectId);
        boolean reverseExists = existing.stream().anyMatch(item ->
                item.getPredecessorTaskId().equals(successorTaskId)
                        && item.getSuccessorTaskId().equals(predecessorTaskId));
        if (reverseExists) {
            throw new IllegalArgumentException("task dependency cycle detected");
        }
    }

    private TaskTypeEnum parseTaskType(String taskType) {
        String value = taskType == null || taskType.isBlank() ? TaskTypeEnum.TASK.name() : taskType;
        try {
            return TaskTypeEnum.valueOf(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid task type");
        }
    }

    private PriorityEnum parsePriority(String priority) {
        try {
            return PriorityEnum.valueOf(priority);
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid priority");
        }
    }

    private TaskStatusEnum parseStatus(String status) {
        try {
            return TaskStatusEnum.valueOf(status);
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid task status");
        }
    }

    private DependencyTypeEnum parseDependencyType(String dependencyType) {
        try {
            return DependencyTypeEnum.valueOf(dependencyType);
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid dependency type");
        }
    }

    private BigDecimal normalizeProgress(BigDecimal progress, String status) {
        BigDecimal value = progress == null ? BigDecimal.ZERO : progress;
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("progress must be between 0 and 100");
        }
        return TaskStatusEnum.DONE.name().equals(status) ? new BigDecimal("100") : value;
    }
}
