package com.manage.managesystem.controller;

import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.service.ProjectViewService;
import com.manage.managesystem.vo.CalendarEventVO;
import com.manage.managesystem.vo.ClosureCheckVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProjectViewController {
    private final ProjectViewService projectViewService;

    public ProjectViewController(ProjectViewService projectViewService) {
        this.projectViewService = projectViewService;
    }

    @GetMapping("/projects/{projectId}/calendar")
    public ApiResponse<List<CalendarEventVO>> projectCalendar(@PathVariable Long projectId) {
        return ApiResponse.success(projectViewService.projectCalendar(projectId));
    }

    @GetMapping("/calendar/my")
    public ApiResponse<List<CalendarEventVO>> myCalendar() {
        return ApiResponse.success(projectViewService.myCalendar());
    }

    @GetMapping("/projects/{projectId}/closure-check")
    public ApiResponse<ClosureCheckVO> closureCheck(@PathVariable Long projectId) {
        return ApiResponse.success(projectViewService.closureCheck(projectId));
    }
}
