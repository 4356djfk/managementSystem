package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.dto.CreateScopeBaselineDto;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.ScopeBaselineService;
import com.manage.managesystem.vo.ScopeBaselineVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/scope-baselines")
public class ScopeBaselineController {
    private final ScopeBaselineService scopeBaselineService;

    public ScopeBaselineController(ScopeBaselineService scopeBaselineService) {
        this.scopeBaselineService = scopeBaselineService;
    }

    @GetMapping
    public ApiResponse<List<ScopeBaselineVO>> list(@PathVariable Long projectId) {
        return ApiResponse.success(scopeBaselineService.list(projectId));
    }

    @PostMapping
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<ScopeBaselineVO> create(@PathVariable Long projectId, @Valid @RequestBody CreateScopeBaselineDto dto) {
        return ApiResponse.success(scopeBaselineService.create(projectId, dto));
    }

    @DeleteMapping("/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> delete(@PathVariable Long projectId, @PathVariable Long id) {
        scopeBaselineService.delete(projectId, id);
        return ApiResponse.success(null);
    }
}
