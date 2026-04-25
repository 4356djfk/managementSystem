package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.CreateTimesheetDto;
import com.manage.managesystem.dto.TimesheetQueryDto;
import com.manage.managesystem.dto.UpdateTimesheetDto;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.TimesheetService;
import com.manage.managesystem.vo.TimesheetVO;
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
@RequestMapping("/projects/{projectId}/timesheets")
public class TimesheetController {
    private final TimesheetService timesheetService;

    public TimesheetController(TimesheetService timesheetService) {
        this.timesheetService = timesheetService;
    }

    @GetMapping
    public ApiResponse<PageResult<TimesheetVO>> list(@PathVariable Long projectId, TimesheetQueryDto queryDto) {
        return ApiResponse.success(timesheetService.list(projectId, queryDto));
    }

    @PostMapping
    public ApiResponse<TimesheetVO> create(@PathVariable Long projectId, @Valid @RequestBody CreateTimesheetDto dto) {
        return ApiResponse.success(timesheetService.create(projectId, dto));
    }

    @PutMapping("/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER, SystemRoleEnum.TEAM_MEMBER})
    public ApiResponse<TimesheetVO> update(@PathVariable Long projectId, @PathVariable Long id, @Valid @RequestBody UpdateTimesheetDto dto) {
        return ApiResponse.success(timesheetService.update(projectId, id, dto));
    }

    @DeleteMapping("/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER, SystemRoleEnum.TEAM_MEMBER})
    public ApiResponse<Void> delete(@PathVariable Long projectId, @PathVariable Long id) {
        timesheetService.delete(projectId, id);
        return ApiResponse.success(null);
    }
}
