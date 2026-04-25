package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.dto.CreateMilestoneDto;
import com.manage.managesystem.dto.UpdateMilestoneDto;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.MilestoneService;
import com.manage.managesystem.vo.MilestoneVO;
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
@RequestMapping("/projects/{projectId}/milestones")
public class MilestoneController {
    private final MilestoneService milestoneService;

    public MilestoneController(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    @GetMapping
    public ApiResponse<List<MilestoneVO>> list(@PathVariable Long projectId) {
        return ApiResponse.success(milestoneService.list(projectId));
    }

    @PostMapping
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<MilestoneVO> create(@PathVariable Long projectId, @Valid @RequestBody CreateMilestoneDto dto) {
        return ApiResponse.success(milestoneService.create(projectId, dto));
    }

    @PutMapping("/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<MilestoneVO> update(@PathVariable Long projectId, @PathVariable Long id, @Valid @RequestBody UpdateMilestoneDto dto) {
        return ApiResponse.success(milestoneService.update(projectId, id, dto));
    }

    @DeleteMapping("/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<Void> delete(@PathVariable Long projectId, @PathVariable Long id) {
        milestoneService.delete(projectId, id);
        return ApiResponse.success(null);
    }
}
