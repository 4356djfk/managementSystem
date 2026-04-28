package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.CreateRiskDto;
import com.manage.managesystem.dto.RiskQueryDto;
import com.manage.managesystem.dto.UpdateRiskDto;
import com.manage.managesystem.dto.UpdateRiskStatusDto;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.RiskService;
import com.manage.managesystem.vo.RiskVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}/risks")
public class RiskController {
    private final RiskService riskService;

    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    @GetMapping
    public ApiResponse<PageResult<RiskVO>> list(@PathVariable Long projectId, RiskQueryDto queryDto) {
        return ApiResponse.success(riskService.list(projectId, queryDto));
    }

    @PostMapping
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<RiskVO> create(@PathVariable Long projectId, @Valid @RequestBody CreateRiskDto dto) {
        return ApiResponse.success(riskService.create(projectId, dto));
    }

    @PutMapping("/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<RiskVO> update(@PathVariable Long projectId, @PathVariable Long id, @Valid @RequestBody UpdateRiskDto dto) {
        return ApiResponse.success(riskService.update(projectId, id, dto));
    }

    @PatchMapping("/{id}/status")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<RiskVO> updateStatus(@PathVariable Long projectId, @PathVariable Long id, @Valid @RequestBody UpdateRiskStatusDto dto) {
        return ApiResponse.success(riskService.updateStatus(projectId, id, dto));
    }

    @DeleteMapping("/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> delete(@PathVariable Long projectId, @PathVariable Long id) {
        riskService.delete(projectId, id);
        return ApiResponse.success(null);
    }
}
