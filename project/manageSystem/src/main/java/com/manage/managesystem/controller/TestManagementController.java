package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.CreateDefectDto;
import com.manage.managesystem.dto.CreateTestCaseDto;
import com.manage.managesystem.dto.CreateTestPlanDto;
import com.manage.managesystem.dto.DefectQueryDto;
import com.manage.managesystem.dto.GenerateReportDto;
import com.manage.managesystem.dto.TestCaseQueryDto;
import com.manage.managesystem.dto.TestPlanQueryDto;
import com.manage.managesystem.dto.UpdateDefectDto;
import com.manage.managesystem.dto.UpdateTestCaseDto;
import com.manage.managesystem.dto.UpdateTestPlanDto;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.TestManagementService;
import com.manage.managesystem.vo.DefectVO;
import com.manage.managesystem.vo.ReportVO;
import com.manage.managesystem.vo.TestCaseVO;
import com.manage.managesystem.vo.TestPlanVO;
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
@RequestMapping("/projects/{projectId}")
public class TestManagementController {
    private final TestManagementService testManagementService;

    public TestManagementController(TestManagementService testManagementService) {
        this.testManagementService = testManagementService;
    }

    @GetMapping("/test-plans")
    public ApiResponse<PageResult<TestPlanVO>> listPlans(@PathVariable Long projectId, TestPlanQueryDto queryDto) {
        return ApiResponse.success(testManagementService.listPlans(projectId, queryDto));
    }

    @PostMapping("/test-plans")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<TestPlanVO> createPlan(@PathVariable Long projectId, @Valid @RequestBody CreateTestPlanDto dto) {
        return ApiResponse.success(testManagementService.createPlan(projectId, dto));
    }

    @PutMapping("/test-plans/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<TestPlanVO> updatePlan(@PathVariable Long projectId,
                                              @PathVariable Long id,
                                              @Valid @RequestBody UpdateTestPlanDto dto) {
        return ApiResponse.success(testManagementService.updatePlan(projectId, id, dto));
    }

    @DeleteMapping("/test-plans/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> deletePlan(@PathVariable Long projectId, @PathVariable Long id) {
        testManagementService.deletePlan(projectId, id);
        return ApiResponse.success(null);
    }

    @GetMapping("/test-cases")
    public ApiResponse<PageResult<TestCaseVO>> listCases(@PathVariable Long projectId, TestCaseQueryDto queryDto) {
        return ApiResponse.success(testManagementService.listCases(projectId, queryDto));
    }

    @PostMapping("/test-cases")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<TestCaseVO> createCase(@PathVariable Long projectId, @Valid @RequestBody CreateTestCaseDto dto) {
        return ApiResponse.success(testManagementService.createCase(projectId, dto));
    }

    @PutMapping("/test-cases/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<TestCaseVO> updateCase(@PathVariable Long projectId,
                                              @PathVariable Long id,
                                              @Valid @RequestBody UpdateTestCaseDto dto) {
        return ApiResponse.success(testManagementService.updateCase(projectId, id, dto));
    }

    @DeleteMapping("/test-cases/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> deleteCase(@PathVariable Long projectId, @PathVariable Long id) {
        testManagementService.deleteCase(projectId, id);
        return ApiResponse.success(null);
    }

    @GetMapping("/defects")
    public ApiResponse<PageResult<DefectVO>> listDefects(@PathVariable Long projectId, DefectQueryDto queryDto) {
        return ApiResponse.success(testManagementService.listDefects(projectId, queryDto));
    }

    @PostMapping("/defects")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<DefectVO> createDefect(@PathVariable Long projectId, @Valid @RequestBody CreateDefectDto dto) {
        return ApiResponse.success(testManagementService.createDefect(projectId, dto));
    }

    @PutMapping("/defects/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<DefectVO> updateDefect(@PathVariable Long projectId,
                                              @PathVariable Long id,
                                              @Valid @RequestBody UpdateDefectDto dto) {
        return ApiResponse.success(testManagementService.updateDefect(projectId, id, dto));
    }

    @DeleteMapping("/defects/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> deleteDefect(@PathVariable Long projectId, @PathVariable Long id) {
        testManagementService.deleteDefect(projectId, id);
        return ApiResponse.success(null);
    }

    @GetMapping("/test-reports")
    public ApiResponse<List<ReportVO>> listReports(@PathVariable Long projectId) {
        return ApiResponse.success(testManagementService.listTestReports(projectId));
    }

    @PostMapping("/test-reports/generate")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<ReportVO> generateReport(@PathVariable Long projectId, @Valid @RequestBody GenerateReportDto dto) {
        return ApiResponse.success(testManagementService.generateTestReport(projectId, dto));
    }

    @DeleteMapping("/test-reports/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> deleteReport(@PathVariable Long projectId, @PathVariable Long id) {
        testManagementService.deleteTestReport(projectId, id);
        return ApiResponse.success(null);
    }
}
