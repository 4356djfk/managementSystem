package com.manage.managesystem.service;

import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.entity.TaskEntity;
import com.manage.managesystem.enums.TaskStatusEnum;
import com.manage.managesystem.mapper.TaskMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class TaskProgressRollupService {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal HUNDRED = new BigDecimal("100.00");

    private final TaskMapper taskMapper;

    public TaskProgressRollupService(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    public Map<Long, RollupSnapshot> calculateRollups(Long projectId) {
        return buildRollups(taskMapper.selectEntitiesByProjectId(projectId));
    }

    public void syncAll(Long projectId) {
        List<TaskEntity> tasks = taskMapper.selectEntitiesByProjectId(projectId);
        if (tasks.isEmpty()) {
            return;
        }
        TaskTree tree = buildTree(tasks);
        Map<Long, RollupSnapshot> rollups = resolveAll(tree);
        Set<Long> taskIds = new LinkedHashSet<>(tree.tasksById.keySet());
        persistDerivedRollups(projectId, tree, rollups, taskIds);
    }

    public void syncAffected(Long projectId, Collection<Long> seedTaskIds) {
        if (seedTaskIds == null || seedTaskIds.isEmpty()) {
            return;
        }
        List<TaskEntity> tasks = taskMapper.selectEntitiesByProjectId(projectId);
        if (tasks.isEmpty()) {
            return;
        }
        TaskTree tree = buildTree(tasks);
        Map<Long, RollupSnapshot> rollups = resolveAll(tree);
        Set<Long> taskIds = new LinkedHashSet<>();
        for (Long seedTaskId : seedTaskIds) {
            collectSelfAndAncestors(tree, seedTaskId, taskIds);
        }
        persistDerivedRollups(projectId, tree, rollups, taskIds);
    }

    private void persistDerivedRollups(Long projectId,
                                       TaskTree tree,
                                       Map<Long, RollupSnapshot> rollups,
                                       Set<Long> taskIds) {
        if (taskIds.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        Long updatedBy = UserContextHolder.getUserId();
        for (Long taskId : taskIds) {
            TaskEntity task = tree.tasksById.get(taskId);
            RollupSnapshot snapshot = rollups.get(taskId);
            if (task == null || snapshot == null || !snapshot.derived()) {
                continue;
            }
            if (Objects.equals(normalizeStatus(task.getStatus()), snapshot.status())
                    && compareProgress(task.getProgress(), snapshot.progress())) {
                continue;
            }
            taskMapper.updateDerivedProgress(
                    projectId,
                    taskId,
                    snapshot.status(),
                    snapshot.progress(),
                    updatedBy,
                    now
            );
        }
    }

    private void collectSelfAndAncestors(TaskTree tree, Long taskId, Set<Long> collector) {
        if (taskId == null) {
            return;
        }
        Long currentId = taskId;
        Set<Long> visited = new HashSet<>();
        while (currentId != null && visited.add(currentId)) {
            collector.add(currentId);
            TaskEntity entity = tree.tasksById.get(currentId);
            currentId = entity == null ? null : entity.getParentTaskId();
        }
    }

    private Map<Long, RollupSnapshot> buildRollups(List<TaskEntity> tasks) {
        if (tasks.isEmpty()) {
            return Map.of();
        }
        return resolveAll(buildTree(tasks));
    }

    private Map<Long, RollupSnapshot> resolveAll(TaskTree tree) {
        Map<Long, RollupSnapshot> rollups = new HashMap<>();
        Set<Long> visiting = new HashSet<>();
        for (Long taskId : tree.tasksById.keySet()) {
            resolveRollup(taskId, tree, rollups, visiting);
        }
        return rollups;
    }

    private RollupSnapshot resolveRollup(Long taskId,
                                         TaskTree tree,
                                         Map<Long, RollupSnapshot> rollups,
                                         Set<Long> visiting) {
        RollupSnapshot existing = rollups.get(taskId);
        if (existing != null) {
            return existing;
        }
        TaskEntity entity = tree.tasksById.get(taskId);
        if (entity == null) {
            return new RollupSnapshot(TaskStatusEnum.TODO.name(), ZERO, false);
        }
        if (!visiting.add(taskId)) {
            RollupSnapshot snapshot = leafSnapshot(entity);
            rollups.put(taskId, snapshot);
            return snapshot;
        }
        List<TaskEntity> children = tree.childrenByParentId.get(taskId);
        RollupSnapshot snapshot;
        if (children == null || children.isEmpty()) {
            snapshot = leafSnapshot(entity);
        } else {
            List<RollupSnapshot> childSnapshots = new ArrayList<>(children.size());
            for (TaskEntity child : children) {
                childSnapshots.add(resolveRollup(child.getId(), tree, rollups, visiting));
            }
            BigDecimal progress = calculateParentProgress(children, childSnapshots);
            snapshot = new RollupSnapshot(calculateParentStatus(childSnapshots), progress, true);
        }
        visiting.remove(taskId);
        rollups.put(taskId, snapshot);
        return snapshot;
    }

    private RollupSnapshot leafSnapshot(TaskEntity entity) {
        BigDecimal progress = normalizeProgress(entity.getProgress());
        return new RollupSnapshot(
                normalizeLeafStatus(entity.getStatus(), progress),
                progress,
                false
        );
    }

    private BigDecimal calculateParentProgress(List<TaskEntity> children, List<RollupSnapshot> childSnapshots) {
        BigDecimal totalWeight = ZERO;
        BigDecimal weightedSum = ZERO;
        BigDecimal plainSum = ZERO;
        for (int i = 0; i < childSnapshots.size(); i++) {
            RollupSnapshot childSnapshot = childSnapshots.get(i);
            BigDecimal progress = normalizeProgress(childSnapshot.progress());
            plainSum = plainSum.add(progress);
            BigDecimal weight = normalizeWeight(children.get(i).getPlannedHours());
            if (weight.compareTo(BigDecimal.ZERO) > 0) {
                totalWeight = totalWeight.add(weight);
                weightedSum = weightedSum.add(progress.multiply(weight));
            }
        }
        BigDecimal progress;
        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            progress = clamp(weightedSum.divide(totalWeight, 2, RoundingMode.HALF_UP));
        } else {
            progress = clamp(plainSum.divide(BigDecimal.valueOf(childSnapshots.size()), 2, RoundingMode.HALF_UP));
        }
        boolean hasIncompleteChild = childSnapshots.stream()
                .anyMatch(item -> normalizeProgress(item.progress()).compareTo(HUNDRED) < 0);
        if (hasIncompleteChild && progress.compareTo(HUNDRED) >= 0) {
            return new BigDecimal("99.00");
        }
        return progress;
    }

    private String calculateParentStatus(List<RollupSnapshot> childSnapshots) {
        boolean allDone = childSnapshots.stream().allMatch(item -> TaskStatusEnum.DONE.name().equals(item.status()));
        if (allDone) {
            return TaskStatusEnum.DONE.name();
        }
        boolean allTodo = childSnapshots.stream().allMatch(item -> TaskStatusEnum.TODO.name().equals(item.status()));
        if (allTodo) {
            return TaskStatusEnum.TODO.name();
        }
        boolean hasPendingReview = childSnapshots.stream().anyMatch(item -> TaskStatusEnum.PENDING_REVIEW.name().equals(item.status()));
        boolean hasBlocked = childSnapshots.stream().anyMatch(item -> TaskStatusEnum.BLOCKED.name().equals(item.status()));
        boolean hasInProgress = childSnapshots.stream().anyMatch(item -> TaskStatusEnum.IN_PROGRESS.name().equals(item.status()));
        boolean hasTodo = childSnapshots.stream().anyMatch(item -> TaskStatusEnum.TODO.name().equals(item.status()));
        if (hasPendingReview && !hasTodo && !hasBlocked && !hasInProgress) {
            return TaskStatusEnum.PENDING_REVIEW.name();
        }
        if (hasBlocked && !hasInProgress) {
            return TaskStatusEnum.BLOCKED.name();
        }
        return TaskStatusEnum.IN_PROGRESS.name();
    }

    private TaskTree buildTree(List<TaskEntity> tasks) {
        Map<Long, TaskEntity> tasksById = new HashMap<>();
        Map<Long, List<TaskEntity>> childrenByParentId = new HashMap<>();
        for (TaskEntity task : tasks) {
            tasksById.put(task.getId(), task);
        }
        for (TaskEntity task : tasks) {
            if (task.getParentTaskId() == null || !tasksById.containsKey(task.getParentTaskId())) {
                continue;
            }
            childrenByParentId.computeIfAbsent(task.getParentTaskId(), key -> new ArrayList<>()).add(task);
        }
        return new TaskTree(tasksById, childrenByParentId);
    }

    private BigDecimal normalizeWeight(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            return ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeProgress(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }
        return clamp(value.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal clamp(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return ZERO;
        }
        if (value.compareTo(HUNDRED) > 0) {
            return HUNDRED;
        }
        return value;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return TaskStatusEnum.TODO.name();
        }
        return status.trim().toUpperCase();
    }

    private String normalizeLeafStatus(String status, BigDecimal progress) {
        if (progress.compareTo(BigDecimal.ZERO) <= 0) {
            return TaskStatusEnum.TODO.name();
        }
        if (TaskStatusEnum.PENDING_REVIEW.name().equals(normalizeStatus(status))
                && progress.compareTo(HUNDRED) >= 0) {
            return TaskStatusEnum.PENDING_REVIEW.name();
        }
        if (progress.compareTo(HUNDRED) >= 0) {
            return TaskStatusEnum.DONE.name();
        }
        return TaskStatusEnum.BLOCKED.name().equals(normalizeStatus(status))
                ? TaskStatusEnum.BLOCKED.name()
                : TaskStatusEnum.IN_PROGRESS.name();
    }

    private boolean compareProgress(BigDecimal left, BigDecimal right) {
        return normalizeProgress(left).compareTo(normalizeProgress(right)) == 0;
    }

    public record RollupSnapshot(String status, BigDecimal progress, boolean derived) {
    }

    private record TaskTree(Map<Long, TaskEntity> tasksById,
                            Map<Long, List<TaskEntity>> childrenByParentId) {
    }
}
