package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.dto.CreateWbsNodeDto;
import com.manage.managesystem.dto.UpdateWbsNodeDto;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.WbsService;
import com.manage.managesystem.vo.WbsNodeVO;
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
@RequestMapping("/projects/{projectId}/wbs")
public class WbsController {
    private final WbsService wbsService;

    public WbsController(WbsService wbsService) {
        this.wbsService = wbsService;
    }

    @GetMapping
    public ApiResponse<List<WbsNodeVO>> list(@PathVariable Long projectId) {
        return ApiResponse.success(wbsService.list(projectId));
    }

    @PostMapping
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<WbsNodeVO> create(@PathVariable Long projectId, @Valid @RequestBody CreateWbsNodeDto dto) {
        return ApiResponse.success(wbsService.create(projectId, dto));
    }

    @PutMapping("/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<WbsNodeVO> update(@PathVariable Long projectId, @PathVariable Long id, @Valid @RequestBody UpdateWbsNodeDto dto) {
        return ApiResponse.success(wbsService.update(projectId, id, dto));
    }

    @DeleteMapping("/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> delete(@PathVariable Long projectId, @PathVariable Long id) {
        wbsService.delete(projectId, id);
        return ApiResponse.success(null);
    }
}
