package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.CreateProjectTemplateDto;
import com.manage.managesystem.dto.UpdateProjectTemplateDto;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.ProjectTemplateService;
import com.manage.managesystem.vo.ProjectTemplateVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/project-templates")
public class ProjectTemplateController {
    private final ProjectTemplateService projectTemplateService;

    public ProjectTemplateController(ProjectTemplateService projectTemplateService) {
        this.projectTemplateService = projectTemplateService;
    }

    @GetMapping
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<PageResult<ProjectTemplateVO>> list(@RequestParam(required = false) String type,
                                                           @RequestParam(required = false) String status,
                                                           @RequestParam(required = false) Integer page,
                                                           @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.success(projectTemplateService.list(type, status, page, pageSize));
    }

    @PostMapping
    @RequireRole({SystemRoleEnum.SYS_ADMIN})
    public ApiResponse<ProjectTemplateVO> create(@Valid @RequestBody CreateProjectTemplateDto dto) {
        return ApiResponse.success(projectTemplateService.create(dto));
    }

    @GetMapping("/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<ProjectTemplateVO> detail(@PathVariable Long id) {
        return ApiResponse.success(projectTemplateService.detail(id));
    }

    @PutMapping("/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN})
    public ApiResponse<ProjectTemplateVO> update(@PathVariable Long id, @Valid @RequestBody UpdateProjectTemplateDto dto) {
        return ApiResponse.success(projectTemplateService.update(id, dto));
    }
}
