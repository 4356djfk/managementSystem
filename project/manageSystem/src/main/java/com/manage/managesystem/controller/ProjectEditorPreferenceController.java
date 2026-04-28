package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.dto.SaveProjectEditorPreferenceDto;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.ProjectEditorPreferenceService;
import com.manage.managesystem.vo.ProjectEditorPreferenceVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProjectEditorPreferenceController {
    private final ProjectEditorPreferenceService projectEditorPreferenceService;

    public ProjectEditorPreferenceController(ProjectEditorPreferenceService projectEditorPreferenceService) {
        this.projectEditorPreferenceService = projectEditorPreferenceService;
    }

    @GetMapping("/projects/{projectId}/editor-preferences")
    public ApiResponse<ProjectEditorPreferenceVO> get(@PathVariable Long projectId) {
        return ApiResponse.success(projectEditorPreferenceService.get(projectId));
    }

    @PutMapping("/projects/{projectId}/editor-preferences")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<ProjectEditorPreferenceVO> save(@PathVariable Long projectId,
                                                       @RequestBody(required = false) SaveProjectEditorPreferenceDto dto) {
        return ApiResponse.success(projectEditorPreferenceService.save(projectId, dto));
    }
}
