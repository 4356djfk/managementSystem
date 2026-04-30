package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.dto.CreateTaskDependencyDto;
import com.manage.managesystem.dto.CreateTaskCommentDto;
import com.manage.managesystem.dto.CreateTaskDto;
import com.manage.managesystem.dto.UpdateTaskDto;
import com.manage.managesystem.dto.UpdateTaskProgressDto;
import com.manage.managesystem.entity.CommentEntity;
import com.manage.managesystem.entity.ProjectEntity;
import com.manage.managesystem.entity.TaskDependencyEntity;
import com.manage.managesystem.entity.TaskEntity;
import com.manage.managesystem.enums.DependencyTypeEnum;
import com.manage.managesystem.enums.PriorityEnum;
import com.manage.managesystem.enums.TaskConstraintTypeEnum;
import com.manage.managesystem.enums.TaskStatusEnum;
import com.manage.managesystem.enums.TaskTypeEnum;
import com.manage.managesystem.mapper.ProjectMapper;
import com.manage.managesystem.mapper.TaskMapper;
import com.manage.managesystem.mapper.UserMapper;
import com.manage.managesystem.vo.TaskDependencyVO;
import com.manage.managesystem.vo.TaskDetailVO;
import com.manage.managesystem.vo.CommentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class TaskCommandService {
    private final TaskMapper taskMapper;
    private final TaskQueryService taskQueryService;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;
    private final ProjectPermissionService projectPermissionService;
    private final NotificationService notificationService;
    private final TaskProgressRollupService taskProgressRollupService;

    public TaskCommandService(TaskMapper taskMapper,
                              TaskQueryService taskQueryService,
                              ProjectMapper projectMapper,
                              UserMapper userMapper,
                              ProjectPermissionService projectPermissionService,
                              NotificationService notificationService,
                              TaskProgressRollupService taskProgressRollupService) {
        this.taskMapper = taskMapper;
        this.taskQueryService = taskQueryService;
        this.projectMapper = projectMapper;
        this.userMapper = userMapper;
        this.projectPermissionService = projectPermissionService;
        this.notificationService = notificationService;
        this.taskProgressRollupService = taskProgressRollupService;
    }

    @Transactional
    public TaskDetailVO create(Long projectId, CreateTaskDto dto) {
        projectPermissionService.ensureProjectOwner(projectId);
        ensureProjectExists(projectId);
        validateTaskRelations(projectId, null, dto.getParentTaskId(), dto.getAssigneeId());
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
        String requestedStatus = dto.getStatus() == null || dto.getStatus().isBlank()
                ? TaskStatusEnum.TODO.name()
                : dto.getStatus();
        validateExplicitCompletionStatus(dto.getProgress(), dto.getStatus());
        TaskStatusEnum taskStatus = normalizeStatus(dto.getProgress(), requestedStatus, true);
        entity.setStatus(taskStatus.name());
        entity.setProgress(normalizeProgress(dto.getProgress(), taskStatus));
        entity.setAssigneeId(resolveAssigneeForStatus(dto.getAssigneeId(), taskStatus));
        validateTaskScheduleFields(
                dto.getPlannedStartDate(),
                dto.getPlannedEndDate(),
                dto.getDeadlineDate(),
                dto.getConstraintType(),
                dto.getConstraintDate()
        );
        entity.setPlannedStartDate(dto.getPlannedStartDate());
        entity.setPlannedEndDate(dto.getPlannedEndDate());
        entity.setDeadlineDate(dto.getDeadlineDate());
        entity.setConstraintType(parseNullableConstraintType(dto.getConstraintType()));
        entity.setConstraintDate(dto.getConstraintDate());
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
        taskProgressRollupService.syncAffected(projectId, List.of(entity.getId()));
        notificationService.notifyTaskAssigned(entity.getAssigneeId(), projectId, entity.getId(), entity.getName(), false);
        return taskQueryService.detail(projectId, entity.getId());
    }

    @Transactional
    public TaskDetailVO update(Long projectId, Long taskId, UpdateTaskDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);
        TaskEntity entity = requireTask(projectId, taskId);
        boolean canManageTask = projectPermissionService.isProjectOwner(projectId);
        ensureTaskUpdatePermission(projectId, entity, canManageTask);
        Long originalParentTaskId = entity.getParentTaskId();
        Long originalAssigneeId = entity.getAssigneeId();
        Long targetParentTaskId = canManageTask ? dto.getParentTaskId() : entity.getParentTaskId();
        Long targetAssigneeId = canManageTask ? dto.getAssigneeId() : entity.getAssigneeId();
        validateTaskRelations(projectId, taskId, targetParentTaskId, targetAssigneeId);
        entity.setParentTaskId(targetParentTaskId);
        entity.setWbsId(canManageTask ? dto.getWbsId() : entity.getWbsId());
        entity.setMilestoneId(canManageTask ? dto.getMilestoneId() : entity.getMilestoneId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setTaskType(canManageTask ? parseTaskType(dto.getTaskType()).name() : entity.getTaskType());
        entity.setPriority(canManageTask ? parsePriority(dto.getPriority()).name() : entity.getPriority());
        String requestedStatus = canManageTask
                ? (dto.getStatus() == null || dto.getStatus().isBlank() ? entity.getStatus() : dto.getStatus())
                : entity.getStatus();
        BigDecimal requestedProgress = canManageTask
                ? (dto.getProgress() == null ? entity.getProgress() : dto.getProgress())
                : entity.getProgress();
        if (canManageTask) {
            validateExplicitCompletionStatus(dto.getProgress(), dto.getStatus());
        }
        TaskStatusEnum normalizedStatus = normalizeStatus(requestedProgress, requestedStatus, canManageTask);
        entity.setStatus(normalizedStatus.name());
        entity.setProgress(normalizeProgress(requestedProgress, normalizedStatus));
        entity.setAssigneeId(resolveAssigneeForStatus(targetAssigneeId, normalizedStatus));
        validateTaskScheduleFields(
                dto.getPlannedStartDate(),
                dto.getPlannedEndDate(),
                canManageTask ? dto.getDeadlineDate() : entity.getDeadlineDate(),
                canManageTask ? dto.getConstraintType() : entity.getConstraintType(),
                canManageTask ? dto.getConstraintDate() : entity.getConstraintDate()
        );
        entity.setPlannedStartDate(dto.getPlannedStartDate());
        entity.setPlannedEndDate(dto.getPlannedEndDate());
        entity.setDeadlineDate(canManageTask ? dto.getDeadlineDate() : entity.getDeadlineDate());
        entity.setConstraintType(canManageTask ? parseNullableConstraintType(dto.getConstraintType()) : entity.getConstraintType());
        entity.setConstraintDate(canManageTask ? dto.getConstraintDate() : entity.getConstraintDate());
        entity.setPlannedHours(dto.getPlannedHours() == null ? BigDecimal.ZERO : dto.getPlannedHours());
        entity.setSortOrder(canManageTask && dto.getSortOrder() != null ? dto.getSortOrder() : entity.getSortOrder());
        entity.setRemark(canManageTask ? dto.getRemark() : entity.getRemark());
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(LocalDateTime.now());
        taskMapper.update(entity);
        List<Long> affectedTaskIds = new ArrayList<>();
        affectedTaskIds.add(taskId);
        affectedTaskIds.add(originalParentTaskId);
        affectedTaskIds.add(entity.getParentTaskId());
        taskProgressRollupService.syncAffected(projectId, affectedTaskIds);
        if (!Objects.equals(originalAssigneeId, entity.getAssigneeId()) && entity.getAssigneeId() != null) {
            notificationService.notifyTaskAssigned(
                    entity.getAssigneeId(),
                    projectId,
                    entity.getId(),
                    entity.getName(),
                    originalAssigneeId != null
            );
        }
        return taskQueryService.detail(projectId, taskId);
    }

    @Transactional
    public void delete(Long projectId, Long taskId) {
        projectPermissionService.ensureProjectOwner(projectId);
        ensureProjectExists(projectId);
        TaskEntity entity = requireTask(projectId, taskId);
        taskMapper.softDelete(projectId, taskId, UserContextHolder.getUserId(), LocalDateTime.now());
        List<Long> affectedTaskIds = new ArrayList<>();
        affectedTaskIds.add(entity.getParentTaskId());
        taskProgressRollupService.syncAffected(projectId, affectedTaskIds);
    }

    @Transactional
    public TaskDetailVO updateProgress(Long projectId, Long taskId, UpdateTaskProgressDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);
        TaskEntity entity = requireTask(projectId, taskId);
        ensureProgressPermission(projectId, entity);
        boolean canManageTask = projectPermissionService.isProjectOwner(projectId);
        validateExplicitCompletionStatus(dto.getProgress(), dto.getStatus());
        TaskStatusEnum status = normalizeStatus(dto.getProgress(), dto.getStatus(), canManageTask);
        BigDecimal progress = normalizeProgress(dto.getProgress(), status);
        Long nextAssigneeId = resolveAssigneeForStatus(entity.getAssigneeId(), status);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime actualStartDate = status == TaskStatusEnum.TODO ? null : now;
        LocalDateTime actualEndDate = status == TaskStatusEnum.DONE ? now : null;
        taskMapper.updateProgress(projectId, taskId, status.name(), progress, nextAssigneeId, dto.getRemark(),
                actualStartDate, actualEndDate, UserContextHolder.getUserId(), now);
        taskProgressRollupService.syncAffected(projectId, List.of(taskId));
        return taskQueryService.detail(projectId, taskId);
    }

    public List<TaskDependencyVO> listDependencies(Long projectId) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProjectExists(projectId);
        return taskMapper.selectDependenciesByProjectId(projectId);
    }

    @Transactional
    public TaskDependencyVO createDependency(Long projectId, CreateTaskDependencyDto dto) {
        projectPermissionService.ensureProjectOwner(projectId);
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
        entity.setLagDays(normalizeLagDays(dto.getLagDays()));
        entity.setCreatedAt(LocalDateTime.now());
        taskMapper.insertDependency(entity);
        return requireDependency(projectId, entity.getId());
    }

    @Transactional
    public void deleteDependency(Long projectId, Long id) {
        projectPermissionService.ensureProjectOwner(projectId);
        ensureProjectExists(projectId);
        requireDependency(projectId, id);
        taskMapper.deleteDependency(projectId, id);
    }

    @Transactional
    public CommentVO createComment(Long projectId, Long taskId, CreateTaskCommentDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
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
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);
        requireTask(projectId, taskId);
        CommentVO comment = requireComment(projectId, id);
        Long currentUserId = UserContextHolder.getUserId();
        if (!projectPermissionService.isProjectOwner(projectId)
                && (currentUserId == null || !currentUserId.equals(comment.getUserId()))) {
            throw new IllegalArgumentException("no permission to delete this comment");
        }
        taskMapper.softDeleteComment(projectId, id, LocalDateTime.now());
    }

    private void ensureProjectExists(Long projectId) {
        if (projectMapper.selectEntityById(projectId) == null) {
            throw new IllegalArgumentException("project not found");
        }
    }

    private void validateTaskRelations(Long projectId, Long taskId, Long parentTaskId, Long assigneeId) {
        if (parentTaskId != null) {
            if (taskId != null && taskId.equals(parentTaskId)) {
                throw new IllegalArgumentException("task cannot be its own parent");
            }
            ensureNoParentCycle(projectId, taskId, requireTask(projectId, parentTaskId));
        }
        if (assigneeId != null && userMapper.selectById(assigneeId) == null) {
            throw new IllegalArgumentException("task assignee not found: " + assigneeId);
        }
        if (assigneeId != null && !isAssignableProjectUser(projectId, assigneeId)) {
            throw new IllegalArgumentException("task assignee must be an active project member or project owner");
        }
    }

    private void ensureNoParentCycle(Long projectId, Long taskId, TaskEntity parentTask) {
        if (taskId == null || parentTask == null) {
            return;
        }
        TaskEntity current = parentTask;
        while (current != null) {
            if (taskId.equals(current.getId())) {
                throw new IllegalArgumentException("task parent cycle detected");
            }
            Long nextParentId = current.getParentTaskId();
            current = nextParentId == null ? null : taskMapper.selectEntityById(projectId, nextParentId);
        }
    }

    private boolean isAssignableProjectUser(Long projectId, Long userId) {
        return projectPermissionService.isAssignableProjectUser(projectId, userId);
    }

    private void ensureProgressPermission(Long projectId, TaskEntity entity) {
        var authUser = UserContextHolder.get();
        if (authUser == null) {
            throw new IllegalArgumentException("user not authenticated");
        }
        if (projectPermissionService.isProjectOwner(projectId)) {
            return;
        }
        if (TaskStatusEnum.DONE.name().equals(entity.getStatus())) {
            throw new IllegalArgumentException("accepted tasks can only be reopened by the project owner");
        }
        Long effectiveAssigneeId = resolveEffectiveAssigneeForTask(projectId, entity);
        if (authUser.getUserId() != null
                && authUser.getUserId().equals(effectiveAssigneeId)) {
            return;
        }
        throw new IllegalArgumentException("participants can only update progress for their assigned tasks");
    }

    private void ensureTaskUpdatePermission(Long projectId, TaskEntity entity, boolean canManageTask) {
        if (canManageTask) {
            return;
        }
        var authUser = UserContextHolder.get();
        if (authUser == null || authUser.getUserId() == null) {
            throw new IllegalArgumentException("user not authenticated");
        }
        if (TaskStatusEnum.DONE.name().equals(entity.getStatus())) {
            throw new IllegalArgumentException("accepted tasks can only be edited by the project owner");
        }
        Long effectiveAssigneeId = resolveEffectiveAssigneeForTask(projectId, entity);
        if (authUser.getUserId().equals(effectiveAssigneeId)) {
            return;
        }
        throw new IllegalArgumentException("participants can only edit their assigned tasks");
    }

    private Long resolveEffectiveAssigneeForTask(Long projectId, TaskEntity task) {
        if (task == null) {
            return null;
        }
        TaskEntity current = task;
        java.util.HashSet<Long> visited = new java.util.HashSet<>();
        while (current != null && visited.add(current.getId())) {
            if (current.getAssigneeId() != null) {
                return current.getAssigneeId();
            }
            if (current.getParentTaskId() == null) {
                return null;
            }
            current = taskMapper.selectEntityById(projectId, current.getParentTaskId());
        }
        return null;
    }

    private Long resolveAssigneeForStatus(Long assigneeId, TaskStatusEnum status) {
        return assigneeId;
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

    private String parseNullableConstraintType(String constraintType) {
        if (constraintType == null || constraintType.isBlank()) {
            return null;
        }
        try {
            return TaskConstraintTypeEnum.valueOf(constraintType.trim().toUpperCase()).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid task constraint type");
        }
    }

    private void validateTaskScheduleFields(LocalDateTime plannedStartDate,
                                            LocalDateTime plannedEndDate,
                                            LocalDateTime deadlineDate,
                                            String constraintType,
                                            LocalDateTime constraintDate) {
        if (plannedStartDate != null && plannedEndDate != null && plannedEndDate.isBefore(plannedStartDate)) {
            throw new IllegalArgumentException("planned end date cannot be earlier than planned start date");
        }
        String normalizedConstraintType = parseNullableConstraintType(constraintType);
        if (normalizedConstraintType == null && constraintDate != null) {
            throw new IllegalArgumentException("constraint date requires a constraint type");
        }
        if (deadlineDate != null && plannedStartDate != null && deadlineDate.isBefore(plannedStartDate)) {
            throw new IllegalArgumentException("deadline date cannot be earlier than planned start date");
        }
        if (normalizedConstraintType == null) {
            return;
        }

        TaskConstraintTypeEnum type = TaskConstraintTypeEnum.valueOf(normalizedConstraintType);
        boolean requiresConstraintDate = switch (type) {
            case SNET, SNLT, FNET, FNLT, MSO, MFO -> true;
            default -> false;
        };
        if (requiresConstraintDate && constraintDate == null) {
            throw new IllegalArgumentException("selected constraint type requires a constraint date");
        }
        if (!requiresConstraintDate || constraintDate == null) {
            return;
        }

        LocalDate constraintLocalDate = constraintDate.toLocalDate();
        if ((type == TaskConstraintTypeEnum.SNET || type == TaskConstraintTypeEnum.SNLT || type == TaskConstraintTypeEnum.MSO)
                && plannedStartDate == null) {
            throw new IllegalArgumentException("selected constraint type requires a planned start date");
        }
        if ((type == TaskConstraintTypeEnum.FNET || type == TaskConstraintTypeEnum.FNLT || type == TaskConstraintTypeEnum.MFO)
                && plannedEndDate == null) {
            throw new IllegalArgumentException("selected constraint type requires a planned end date");
        }
        if (plannedStartDate != null) {
            LocalDate plannedStartLocalDate = plannedStartDate.toLocalDate();
            if (type == TaskConstraintTypeEnum.SNET && plannedStartLocalDate.isBefore(constraintLocalDate)) {
                throw new IllegalArgumentException("planned start date violates Start No Earlier Than");
            }
            if (type == TaskConstraintTypeEnum.SNLT && plannedStartLocalDate.isAfter(constraintLocalDate)) {
                throw new IllegalArgumentException("planned start date violates Start No Later Than");
            }
            if (type == TaskConstraintTypeEnum.MSO && !plannedStartLocalDate.isEqual(constraintLocalDate)) {
                throw new IllegalArgumentException("planned start date violates Must Start On");
            }
        }
        if (plannedEndDate != null) {
            LocalDate plannedEndLocalDate = plannedEndDate.toLocalDate();
            if (type == TaskConstraintTypeEnum.FNET && plannedEndLocalDate.isBefore(constraintLocalDate)) {
                throw new IllegalArgumentException("planned end date violates Finish No Earlier Than");
            }
            if (type == TaskConstraintTypeEnum.FNLT && plannedEndLocalDate.isAfter(constraintLocalDate)) {
                throw new IllegalArgumentException("planned end date violates Finish No Later Than");
            }
            if (type == TaskConstraintTypeEnum.MFO && !plannedEndLocalDate.isEqual(constraintLocalDate)) {
                throw new IllegalArgumentException("planned end date violates Must Finish On");
            }
        }
    }

    private Integer normalizeLagDays(Integer lagDays) {
        if (lagDays == null) {
            return 0;
        }
        if (lagDays < -3650 || lagDays > 3650) {
            throw new IllegalArgumentException("lag days must be between -3650 and 3650");
        }
        return lagDays;
    }

    private TaskStatusEnum normalizeStatus(BigDecimal progress, String status, boolean canFinalizeTask) {
        String requestedStatus = status == null || status.isBlank() ? TaskStatusEnum.TODO.name() : parseStatus(status).name();
        BigDecimal value = clampProgress(progress);
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            return TaskStatusEnum.TODO;
        }
        if (TaskStatusEnum.PENDING_REVIEW.name().equals(requestedStatus)
                && value.compareTo(new BigDecimal("100")) >= 0) {
            return TaskStatusEnum.PENDING_REVIEW;
        }
        if (value.compareTo(new BigDecimal("100")) >= 0) {
            return canFinalizeTask ? TaskStatusEnum.DONE : TaskStatusEnum.PENDING_REVIEW;
        }
        if (TaskStatusEnum.BLOCKED.name().equals(requestedStatus)) {
            return TaskStatusEnum.BLOCKED;
        }
        return TaskStatusEnum.IN_PROGRESS;
    }

    private void validateExplicitCompletionStatus(BigDecimal progress, String status) {
        if (status == null || status.isBlank()) {
            return;
        }
        TaskStatusEnum requestedStatus = parseStatus(status);
        if (requestedStatus != TaskStatusEnum.PENDING_REVIEW && requestedStatus != TaskStatusEnum.DONE) {
            return;
        }
        if (clampProgress(progress).compareTo(new BigDecimal("100")) < 0) {
            throw new IllegalArgumentException("progress must reach 100 before submitting completion");
        }
    }

    private BigDecimal normalizeProgress(BigDecimal progress, TaskStatusEnum status) {
        BigDecimal value = clampProgress(progress);
        if (status == TaskStatusEnum.TODO) {
            return BigDecimal.ZERO;
        }
        if (status == TaskStatusEnum.DONE || status == TaskStatusEnum.PENDING_REVIEW) {
            return new BigDecimal("100");
        }
        if (value.compareTo(new BigDecimal("100")) >= 0) {
            return new BigDecimal("99");
        }
        return value;
    }

    private BigDecimal clampProgress(BigDecimal progress) {
        BigDecimal value = progress == null ? BigDecimal.ZERO : progress;
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("progress must be between 0 and 100");
        }
        return value;
    }
}
