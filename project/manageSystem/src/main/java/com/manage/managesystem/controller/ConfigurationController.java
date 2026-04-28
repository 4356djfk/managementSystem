package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.BaselineQueryDto;
import com.manage.managesystem.dto.ConfigItemQueryDto;
import com.manage.managesystem.dto.CreateBaselineDto;
import com.manage.managesystem.dto.CreateConfigItemDto;
import com.manage.managesystem.dto.UpdateConfigItemDto;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.ConfigurationService;
import com.manage.managesystem.vo.BaselineRecordVO;
import com.manage.managesystem.vo.ConfigItemVO;
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
public class ConfigurationController {
    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping("/config-items")
    public ApiResponse<PageResult<ConfigItemVO>> listConfigItems(@PathVariable Long projectId, ConfigItemQueryDto queryDto) {
        return ApiResponse.success(configurationService.listConfigItems(projectId, queryDto));
    }

    @PostMapping("/config-items")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<ConfigItemVO> createConfigItem(@PathVariable Long projectId, @Valid @RequestBody CreateConfigItemDto dto) {
        return ApiResponse.success(configurationService.createConfigItem(projectId, dto));
    }

    @PutMapping("/config-items/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<ConfigItemVO> updateConfigItem(@PathVariable Long projectId,
                                                      @PathVariable Long id,
                                                      @Valid @RequestBody UpdateConfigItemDto dto) {
        return ApiResponse.success(configurationService.updateConfigItem(projectId, id, dto));
    }

    @DeleteMapping("/config-items/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> deleteConfigItem(@PathVariable Long projectId, @PathVariable Long id) {
        configurationService.deleteConfigItem(projectId, id);
        return ApiResponse.success(null);
    }

    @GetMapping("/baselines")
    public ApiResponse<PageResult<BaselineRecordVO>> listBaselines(@PathVariable Long projectId, BaselineQueryDto queryDto) {
        return ApiResponse.success(configurationService.listBaselines(projectId, queryDto));
    }

    @PostMapping("/baselines")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<BaselineRecordVO> createBaseline(@PathVariable Long projectId, @Valid @RequestBody CreateBaselineDto dto) {
        return ApiResponse.success(configurationService.createBaseline(projectId, dto));
    }

    @DeleteMapping("/baselines/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> deleteBaseline(@PathVariable Long projectId, @PathVariable Long id) {
        configurationService.deleteBaseline(projectId, id);
        return ApiResponse.success(null);
    }

    @PostMapping("/baselines/{id}/restore")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> restoreBaseline(@PathVariable Long projectId, @PathVariable Long id) {
        configurationService.restoreBaseline(projectId, id);
        return ApiResponse.success(null);
    }
}
