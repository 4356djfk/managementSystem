package com.manage.managesystem.controller;

import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.service.DashboardService;
import com.manage.managesystem.vo.DashboardHomeVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/home")
    public ApiResponse<DashboardHomeVO> home() {
        return ApiResponse.success(dashboardService.home());
    }
}
