package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.CreateUserDto;
import com.manage.managesystem.dto.UpdateUserDto;
import com.manage.managesystem.dto.UpdateUserRolesDto;
import com.manage.managesystem.dto.UpdateUserStatusDto;
import com.manage.managesystem.dto.UserQueryDto;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.UserService;
import com.manage.managesystem.vo.UserListItemVO;
import com.manage.managesystem.vo.UserProfileVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @RequireRole({SystemRoleEnum.SYS_ADMIN})
    public ApiResponse<PageResult<UserListItemVO>> list(UserQueryDto queryDto) {
        return ApiResponse.success(userService.listUsers(queryDto));
    }

    @PostMapping
    @RequireRole({SystemRoleEnum.SYS_ADMIN})
    public ApiResponse<UserProfileVO> create(@Valid @RequestBody CreateUserDto dto) {
        return ApiResponse.success(userService.createUser(dto));
    }

    @PutMapping("/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN})
    public ApiResponse<UserProfileVO> update(@PathVariable Long id, @Valid @RequestBody UpdateUserDto dto) {
        return ApiResponse.success(userService.updateUser(id, dto));
    }

    @PatchMapping("/{id}/status")
    @RequireRole({SystemRoleEnum.SYS_ADMIN})
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateUserStatusDto dto) {
        userService.updateUserStatus(id, dto);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/roles")
    @RequireRole({SystemRoleEnum.SYS_ADMIN})
    public ApiResponse<UserProfileVO> updateRoles(@PathVariable Long id, @Valid @RequestBody UpdateUserRolesDto dto) {
        return ApiResponse.success(userService.updateUserRoles(id, dto));
    }
}
