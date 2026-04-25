package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.ChangeProjectStatusDto;
import com.manage.managesystem.dto.CreateProjectDto;
import com.manage.managesystem.dto.CreateProjectFromTemplateDto;
import com.manage.managesystem.dto.ProjectQueryDto;
import com.manage.managesystem.dto.SaveProjectCharterDto;
import com.manage.managesystem.dto.UpdateProjectDto;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.ProjectService;
import com.manage.managesystem.vo.ProjectCharterVO;
import com.manage.managesystem.vo.ProjectDashboardVO;
import com.manage.managesystem.vo.ProjectDetailVO;
import com.manage.managesystem.vo.ProjectListItemVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ApiResponse<PageResult<ProjectListItemVO>> list(ProjectQueryDto queryDto) {
        return ApiResponse.success(projectService.list(queryDto));
    }

    @GetMapping("/{projectId}")
    public ApiResponse<ProjectDetailVO> detail(@PathVariable Long projectId) {
        return ApiResponse.success(projectService.detail(projectId));
    }

    @PostMapping
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<ProjectDetailVO> create(@Valid @RequestBody CreateProjectDto dto) {
        return ApiResponse.success(projectService.create(dto));
    }

    @PutMapping("/{projectId}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<ProjectDetailVO> update(@PathVariable Long projectId, @Valid @RequestBody UpdateProjectDto dto) {
        return ApiResponse.success(projectService.update(projectId, dto));
    }

    @PatchMapping("/{projectId}/status")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<Void> changeStatus(@PathVariable Long projectId, @Valid @RequestBody ChangeProjectStatusDto dto) {
        projectService.changeStatus(projectId, dto);
        return ApiResponse.success(null);
    }

    @PostMapping("/{projectId}/close")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<Void> close(@PathVariable Long projectId) {
        projectService.close(projectId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{projectId}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<Void> delete(@PathVariable Long projectId) {
        projectService.delete(projectId);
        return ApiResponse.success(null);
    }

    @GetMapping("/{projectId}/charter")
    public ApiResponse<ProjectCharterVO> charter(@PathVariable Long projectId) {
        return ApiResponse.success(projectService.getCharter(projectId));
    }

    @PostMapping("/{projectId}/charter")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<ProjectCharterVO> createCharter(@PathVariable Long projectId, @Valid @RequestBody SaveProjectCharterDto dto) {
        return ApiResponse.success(projectService.saveCharter(projectId, dto));
    }

    @PutMapping("/{projectId}/charter")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<ProjectCharterVO> updateCharter(@PathVariable Long projectId, @Valid @RequestBody SaveProjectCharterDto dto) {
        return ApiResponse.success(projectService.saveCharter(projectId, dto));
    }

    @PostMapping("/from-template")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<ProjectDetailVO> createFromTemplate(@Valid @RequestBody CreateProjectFromTemplateDto dto) {
        return ApiResponse.success(projectService.createFromTemplate(dto));
    }

    @PostMapping("/init-demo")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<ProjectDetailVO> initDemo() {
        return ApiResponse.success(projectService.initDemo());
    }

    @GetMapping("/{projectId}/dashboard")
    public ApiResponse<ProjectDashboardVO> dashboard(@PathVariable Long projectId) {
        return ApiResponse.success(projectService.dashboard(projectId));
    }
}
