package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.CreateTaskDependencyDto;
import com.manage.managesystem.dto.CreateTaskCommentDto;
import com.manage.managesystem.dto.CreateTaskDto;
import com.manage.managesystem.dto.TaskQueryDto;
import com.manage.managesystem.dto.UpdateTaskDto;
import com.manage.managesystem.dto.UpdateTaskProgressDto;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.TaskCommandService;
import com.manage.managesystem.service.TaskQueryService;
import com.manage.managesystem.vo.GanttTaskVO;
import com.manage.managesystem.vo.ScheduleAlertVO;
import com.manage.managesystem.vo.CommentVO;
import com.manage.managesystem.vo.TaskDependencyVO;
import com.manage.managesystem.vo.TaskDetailVO;
import com.manage.managesystem.vo.TaskListItemVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TaskController {
    private final TaskQueryService taskQueryService;
    private final TaskCommandService taskCommandService;

    public TaskController(TaskQueryService taskQueryService, TaskCommandService taskCommandService) {
        this.taskQueryService = taskQueryService;
        this.taskCommandService = taskCommandService;
    }

    @GetMapping("/projects/{projectId}/tasks")
    public ApiResponse<PageResult<TaskListItemVO>> list(@PathVariable Long projectId, TaskQueryDto queryDto) {
        return ApiResponse.success(taskQueryService.list(projectId, queryDto));
    }

    @PostMapping("/projects/{projectId}/tasks")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<TaskDetailVO> create(@PathVariable Long projectId, @Valid @RequestBody CreateTaskDto dto) {
        return ApiResponse.success(taskCommandService.create(projectId, dto));
    }

    @GetMapping("/projects/{projectId}/tasks/{taskId}")
    public ApiResponse<TaskDetailVO> detail(@PathVariable Long projectId, @PathVariable Long taskId) {
        return ApiResponse.success(taskQueryService.detail(projectId, taskId));
    }

    @PutMapping("/projects/{projectId}/tasks/{taskId}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<TaskDetailVO> update(@PathVariable Long projectId, @PathVariable Long taskId, @Valid @RequestBody UpdateTaskDto dto) {
        return ApiResponse.success(taskCommandService.update(projectId, taskId, dto));
    }

    @DeleteMapping("/projects/{projectId}/tasks/{taskId}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> delete(@PathVariable Long projectId, @PathVariable Long taskId) {
        taskCommandService.delete(projectId, taskId);
        return ApiResponse.success(null);
    }

    @PatchMapping("/projects/{projectId}/tasks/{taskId}/progress")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<TaskDetailVO> updateProgress(@PathVariable Long projectId, @PathVariable Long taskId, @Valid @RequestBody UpdateTaskProgressDto dto) {
        return ApiResponse.success(taskCommandService.updateProgress(projectId, taskId, dto));
    }

    @GetMapping("/projects/{projectId}/task-dependencies")
    public ApiResponse<List<TaskDependencyVO>> listDependencies(@PathVariable Long projectId) {
        return ApiResponse.success(taskCommandService.listDependencies(projectId));
    }

    @PostMapping("/projects/{projectId}/task-dependencies")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<TaskDependencyVO> createDependency(@PathVariable Long projectId, @Valid @RequestBody CreateTaskDependencyDto dto) {
        return ApiResponse.success(taskCommandService.createDependency(projectId, dto));
    }

    @DeleteMapping("/projects/{projectId}/task-dependencies/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> deleteDependency(@PathVariable Long projectId, @PathVariable Long id) {
        taskCommandService.deleteDependency(projectId, id);
        return ApiResponse.success(null);
    }

    @PostMapping("/projects/{projectId}/tasks/{taskId}/comments")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<CommentVO> createComment(@PathVariable Long projectId, @PathVariable Long taskId, @Valid @RequestBody CreateTaskCommentDto dto) {
        return ApiResponse.success(taskCommandService.createComment(projectId, taskId, dto));
    }

    @GetMapping("/projects/{projectId}/tasks/{taskId}/comments")
    public ApiResponse<List<CommentVO>> listComments(@PathVariable Long projectId, @PathVariable Long taskId) {
        return ApiResponse.success(taskQueryService.listComments(projectId, taskId));
    }

    @DeleteMapping("/projects/{projectId}/tasks/{taskId}/comments/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> deleteComment(@PathVariable Long projectId, @PathVariable Long taskId, @PathVariable Long id) {
        taskCommandService.deleteComment(projectId, taskId, id);
        return ApiResponse.success(null);
    }

    @GetMapping("/projects/{projectId}/gantt")
    public ApiResponse<List<GanttTaskVO>> gantt(@PathVariable Long projectId) {
        return ApiResponse.success(taskQueryService.gantt(projectId));
    }

    @GetMapping("/projects/{projectId}/critical-path")
    public ApiResponse<List<TaskListItemVO>> criticalPath(@PathVariable Long projectId) {
        return ApiResponse.success(taskQueryService.criticalPath(projectId));
    }

    @GetMapping("/projects/{projectId}/schedule-alerts")
    public ApiResponse<List<ScheduleAlertVO>> scheduleAlerts(@PathVariable Long projectId) {
        return ApiResponse.success(taskQueryService.scheduleAlerts(projectId));
    }
}
