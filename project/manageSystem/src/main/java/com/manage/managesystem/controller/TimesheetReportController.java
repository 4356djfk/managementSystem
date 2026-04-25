package com.manage.managesystem.controller;

import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.service.TimesheetService;
import com.manage.managesystem.vo.TimesheetReportVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}/timesheet-reports")
public class TimesheetReportController {
    private final TimesheetService timesheetService;

    public TimesheetReportController(TimesheetService timesheetService) {
        this.timesheetService = timesheetService;
    }

    @GetMapping
    public ApiResponse<TimesheetReportVO> detail(@PathVariable Long projectId) {
        return ApiResponse.success(timesheetService.report(projectId));
    }
}
