package com.manage.managesystem.controller;

import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.service.RiskService;
import com.manage.managesystem.vo.RiskMatrixVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}/risk-matrix")
public class RiskMatrixController {
    private final RiskService riskService;

    public RiskMatrixController(RiskService riskService) {
        this.riskService = riskService;
    }

    @GetMapping
    public ApiResponse<RiskMatrixVO> detail(@PathVariable Long projectId) {
        return ApiResponse.success(riskService.riskMatrix(projectId));
    }
}
