package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.CreateQualityActivityDto;
import com.manage.managesystem.dto.CreateQualityMetricDto;
import com.manage.managesystem.dto.CreateQualityPlanDto;
import com.manage.managesystem.dto.QualityActivityQueryDto;
import com.manage.managesystem.dto.QualityMetricQueryDto;
import com.manage.managesystem.dto.QualityPlanQueryDto;
import com.manage.managesystem.dto.UpdateQualityActivityDto;
import com.manage.managesystem.dto.UpdateQualityMetricDto;
import com.manage.managesystem.dto.UpdateQualityPlanDto;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.QualityService;
import com.manage.managesystem.vo.QualityActivityVO;
import com.manage.managesystem.vo.QualityMetricVO;
import com.manage.managesystem.vo.QualityPlanVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}")
public class QualityController {
    private final QualityService qualityService;

    public QualityController(QualityService qualityService) {
        this.qualityService = qualityService;
    }

    @GetMapping("/quality-plans")
    public ApiResponse<PageResult<QualityPlanVO>> listPlans(@PathVariable Long projectId, QualityPlanQueryDto queryDto) {
        return ApiResponse.success(qualityService.listPlans(projectId, queryDto));
    }

    @PostMapping("/quality-plans")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<QualityPlanVO> createPlan(@PathVariable Long projectId, @Valid @RequestBody CreateQualityPlanDto dto) {
        return ApiResponse.success(qualityService.createPlan(projectId, dto));
    }

    @PutMapping("/quality-plans/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<QualityPlanVO> updatePlan(@PathVariable Long projectId,
                                                 @PathVariable Long id,
                                                 @Valid @RequestBody UpdateQualityPlanDto dto) {
        return ApiResponse.success(qualityService.updatePlan(projectId, id, dto));
    }

    @DeleteMapping("/quality-plans/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> deletePlan(@PathVariable Long projectId, @PathVariable Long id) {
        qualityService.deletePlan(projectId, id);
        return ApiResponse.success(null);
    }

    @GetMapping("/quality-activities")
    public ApiResponse<PageResult<QualityActivityVO>> listActivities(@PathVariable Long projectId, QualityActivityQueryDto queryDto) {
        return ApiResponse.success(qualityService.listActivities(projectId, queryDto));
    }

    @PostMapping("/quality-activities")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<QualityActivityVO> createActivity(@PathVariable Long projectId, @Valid @RequestBody CreateQualityActivityDto dto) {
        return ApiResponse.success(qualityService.createActivity(projectId, dto));
    }

    @PutMapping("/quality-activities/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<QualityActivityVO> updateActivity(@PathVariable Long projectId,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody UpdateQualityActivityDto dto) {
        return ApiResponse.success(qualityService.updateActivity(projectId, id, dto));
    }

    @DeleteMapping("/quality-activities/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> deleteActivity(@PathVariable Long projectId, @PathVariable Long id) {
        qualityService.deleteActivity(projectId, id);
        return ApiResponse.success(null);
    }

    @GetMapping("/quality-metrics")
    public ApiResponse<PageResult<QualityMetricVO>> listMetrics(@PathVariable Long projectId, QualityMetricQueryDto queryDto) {
        return ApiResponse.success(qualityService.listMetrics(projectId, queryDto));
    }

    @PostMapping("/quality-metrics")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<QualityMetricVO> createMetric(@PathVariable Long projectId, @Valid @RequestBody CreateQualityMetricDto dto) {
        return ApiResponse.success(qualityService.createMetric(projectId, dto));
    }

    @PutMapping("/quality-metrics/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<QualityMetricVO> updateMetric(@PathVariable Long projectId,
                                                     @PathVariable Long id,
                                                     @Valid @RequestBody UpdateQualityMetricDto dto) {
        return ApiResponse.success(qualityService.updateMetric(projectId, id, dto));
    }

    @DeleteMapping("/quality-metrics/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> deleteMetric(@PathVariable Long projectId, @PathVariable Long id) {
        qualityService.deleteMetric(projectId, id);
        return ApiResponse.success(null);
    }
}
