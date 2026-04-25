package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.dto.CreateCostActualDto;
import com.manage.managesystem.dto.CreateCostBaselineDto;
import com.manage.managesystem.dto.CreateCostPlanDto;
import com.manage.managesystem.dto.UpdateCostPlanDto;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.CostService;
import com.manage.managesystem.vo.CostActualVO;
import com.manage.managesystem.vo.CostBaselineVO;
import com.manage.managesystem.vo.CostPlanVO;
import com.manage.managesystem.vo.EvmMetricVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CostController {
    private final CostService costService;

    public CostController(CostService costService) {
        this.costService = costService;
    }

    @GetMapping("/projects/{projectId}/cost-plans")
    public ApiResponse<List<CostPlanVO>> listPlans(@PathVariable Long projectId) {
        return ApiResponse.success(costService.listPlans(projectId));
    }

    @PostMapping("/projects/{projectId}/cost-plans")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<CostPlanVO> createPlan(@PathVariable Long projectId, @Valid @RequestBody CreateCostPlanDto dto) {
        return ApiResponse.success(costService.createPlan(projectId, dto));
    }

    @PutMapping("/projects/{projectId}/cost-plans/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<CostPlanVO> updatePlan(@PathVariable Long projectId, @PathVariable Long id, @Valid @RequestBody UpdateCostPlanDto dto) {
        return ApiResponse.success(costService.updatePlan(projectId, id, dto));
    }

    @DeleteMapping("/projects/{projectId}/cost-plans/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<Void> deletePlan(@PathVariable Long projectId, @PathVariable Long id) {
        costService.deletePlan(projectId, id);
        return ApiResponse.success(null);
    }

    @PostMapping("/projects/{projectId}/cost-baselines")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<CostBaselineVO> createBaseline(@PathVariable Long projectId, @Valid @RequestBody CreateCostBaselineDto dto) {
        return ApiResponse.success(costService.createBaseline(projectId, dto));
    }

    @GetMapping("/projects/{projectId}/cost-baselines")
    public ApiResponse<List<CostBaselineVO>> listBaselines(@PathVariable Long projectId) {
        return ApiResponse.success(costService.listBaselines(projectId));
    }

    @GetMapping("/projects/{projectId}/cost-actuals")
    public ApiResponse<List<CostActualVO>> listActuals(@PathVariable Long projectId) {
        return ApiResponse.success(costService.listActuals(projectId));
    }

    @PostMapping("/projects/{projectId}/cost-actuals")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<CostActualVO> createActual(@PathVariable Long projectId, @Valid @RequestBody CreateCostActualDto dto) {
        return ApiResponse.success(costService.createActual(projectId, dto));
    }

    @DeleteMapping("/projects/{projectId}/cost-actuals/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<Void> deleteActual(@PathVariable Long projectId, @PathVariable Long id) {
        costService.deleteActual(projectId, id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/projects/{projectId}/cost-baselines/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<Void> deleteBaseline(@PathVariable Long projectId, @PathVariable Long id) {
        costService.deleteBaseline(projectId, id);
        return ApiResponse.success(null);
    }

    @GetMapping("/projects/{projectId}/evm")
    public ApiResponse<EvmMetricVO> evm(@PathVariable Long projectId) {
        return ApiResponse.success(costService.evm(projectId));
    }
}
