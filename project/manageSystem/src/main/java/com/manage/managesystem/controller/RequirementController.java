package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.CreateRequirementDto;
import com.manage.managesystem.dto.RequirementQueryDto;
import com.manage.managesystem.dto.UpdateRequirementDto;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.RequirementService;
import com.manage.managesystem.vo.RequirementVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}/requirements")
public class RequirementController {
    private final RequirementService requirementService;

    public RequirementController(RequirementService requirementService) {
        this.requirementService = requirementService;
    }

    @GetMapping
    public ApiResponse<PageResult<RequirementVO>> list(@PathVariable Long projectId, RequirementQueryDto queryDto) {
        return ApiResponse.success(requirementService.list(projectId, queryDto));
    }

    @PostMapping
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<RequirementVO> create(@PathVariable Long projectId, @Valid @RequestBody CreateRequirementDto dto) {
        return ApiResponse.success(requirementService.create(projectId, dto));
    }

    @PutMapping("/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<RequirementVO> update(@PathVariable Long projectId, @PathVariable Long id, @Valid @RequestBody UpdateRequirementDto dto) {
        return ApiResponse.success(requirementService.update(projectId, id, dto));
    }
}
