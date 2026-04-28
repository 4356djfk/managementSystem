package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.dto.AddProjectMemberDto;
import com.manage.managesystem.dto.UpdateProjectMemberDto;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.ProjectMemberService;
import com.manage.managesystem.vo.ProjectMemberCandidateVO;
import com.manage.managesystem.vo.ProjectMemberVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/members")
public class ProjectMemberController {
    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    @GetMapping
    public ApiResponse<List<ProjectMemberVO>> list(@PathVariable Long projectId) {
        return ApiResponse.success(projectMemberService.list(projectId));
    }

    @GetMapping("/candidates")
    public ApiResponse<List<ProjectMemberCandidateVO>> candidates(@PathVariable Long projectId) {
        return ApiResponse.success(projectMemberService.listCandidates(projectId));
    }

    @PostMapping
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<ProjectMemberVO> add(@PathVariable Long projectId, @Valid @RequestBody AddProjectMemberDto dto) {
        return ApiResponse.success(projectMemberService.add(projectId, dto));
    }

    @PutMapping("/{memberId}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<ProjectMemberVO> update(@PathVariable Long projectId,
                                               @PathVariable Long memberId,
                                               @Valid @RequestBody UpdateProjectMemberDto dto) {
        return ApiResponse.success(projectMemberService.update(projectId, memberId, dto));
    }

    @DeleteMapping("/{memberId}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> remove(@PathVariable Long projectId, @PathVariable Long memberId) {
        projectMemberService.remove(projectId, memberId);
        return ApiResponse.success(null);
    }
}
