package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.ApproveChangeRequestDto;
import com.manage.managesystem.dto.ChangeRequestQueryDto;
import com.manage.managesystem.dto.CreateChangeRequestDto;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.ChangeRequestService;
import com.manage.managesystem.vo.ChangeRequestLogVO;
import com.manage.managesystem.vo.ChangeRequestVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/change-requests")
public class ChangeRequestController {
    private final ChangeRequestService changeRequestService;

    public ChangeRequestController(ChangeRequestService changeRequestService) {
        this.changeRequestService = changeRequestService;
    }

    @GetMapping
    public ApiResponse<PageResult<ChangeRequestVO>> list(@PathVariable Long projectId, ChangeRequestQueryDto queryDto) {
        return ApiResponse.success(changeRequestService.list(projectId, queryDto));
    }

    @PostMapping
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER, SystemRoleEnum.TEAM_MEMBER})
    public ApiResponse<ChangeRequestVO> create(@PathVariable Long projectId, @Valid @RequestBody CreateChangeRequestDto dto) {
        return ApiResponse.success(changeRequestService.create(projectId, dto));
    }

    @GetMapping("/{id}")
    public ApiResponse<ChangeRequestVO> detail(@PathVariable Long projectId, @PathVariable Long id) {
        return ApiResponse.success(changeRequestService.detail(projectId, id));
    }

    @PostMapping("/{id}/approve")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<ChangeRequestVO> approve(@PathVariable Long projectId, @PathVariable Long id, @Valid @RequestBody ApproveChangeRequestDto dto) {
        return ApiResponse.success(changeRequestService.approve(projectId, id, dto));
    }

    @GetMapping("/{id}/logs")
    public ApiResponse<List<ChangeRequestLogVO>> logs(@PathVariable Long projectId, @PathVariable Long id) {
        return ApiResponse.success(changeRequestService.logs(projectId, id));
    }
}
