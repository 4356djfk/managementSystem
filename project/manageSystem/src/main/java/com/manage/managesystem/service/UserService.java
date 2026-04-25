package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.auth.PasswordUtils;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.CreateUserDto;
import com.manage.managesystem.dto.UpdateUserDto;
import com.manage.managesystem.dto.UpdateUserRolesDto;
import com.manage.managesystem.dto.UpdateUserStatusDto;
import com.manage.managesystem.dto.UserQueryDto;
import com.manage.managesystem.entity.RoleEntity;
import com.manage.managesystem.entity.UserEntity;
import com.manage.managesystem.entity.UserRoleEntity;
import com.manage.managesystem.enums.UserStatusEnum;
import com.manage.managesystem.mapper.RoleMapper;
import com.manage.managesystem.mapper.UserMapper;
import com.manage.managesystem.mapper.UserRoleMapper;
import com.manage.managesystem.vo.UserListItemVO;
import com.manage.managesystem.vo.UserProfileVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    public UserService(UserMapper userMapper, RoleMapper roleMapper, UserRoleMapper userRoleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
    }

    public PageResult<UserListItemVO> listUsers(UserQueryDto queryDto) {
        List<UserEntity> users = userMapper.selectList(queryDto.getKeyword(), queryDto.getStatus());
        List<UserListItemVO> list = new ArrayList<>();
        for (UserEntity user : users) {
            List<String> roleCodes = userRoleMapper.selectRoleCodesByUserId(user.getId());
            if (queryDto.getRole() != null && !queryDto.getRole().isBlank() && !roleCodes.contains(queryDto.getRole())) {
                continue;
            }
            UserListItemVO vo = new UserListItemVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setEmail(user.getEmail());
            vo.setPhone(user.getPhone());
            vo.setStatus(user.getStatus());
            vo.setRoles(roleCodes);
            vo.setLastLoginAt(user.getLastLoginAt());
            list.add(vo);
        }
        PageResult<UserListItemVO> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setPage(queryDto.getPage() == null ? 1 : queryDto.getPage());
        pageResult.setPageSize(queryDto.getPageSize() == null ? list.size() : queryDto.getPageSize());
        pageResult.setTotal((long) list.size());
        return pageResult;
    }

    @Transactional
    public UserProfileVO createUser(CreateUserDto dto) {
        if (userMapper.selectByUsername(dto.getUsername()) != null) {
            throw new IllegalArgumentException("username already exists");
        }
        List<RoleEntity> roles = validateRoles(dto.getRoleCodes());
        LocalDateTime now = LocalDateTime.now();
        Long operatorId = UserContextHolder.getUserId();

        UserEntity user = new UserEntity();
        user.setId(IdWorker.getId());
        user.setUsername(dto.getUsername());
        user.setPasswordHash(PasswordUtils.hash(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(UserStatusEnum.ACTIVE.name());
        user.setCreatedBy(operatorId);
        user.setCreatedAt(now);
        user.setUpdatedBy(operatorId);
        user.setUpdatedAt(now);
        user.setDeleted(0);
        userMapper.insert(user);

        replaceUserRoles(user.getId(), roles, now);
        return buildUserProfile(user, extractRoleCodes(roles));
    }

    @Transactional
    public UserProfileVO updateUser(Long userId, UpdateUserDto dto) {
        UserEntity user = requireUser(userId);
        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setUpdatedBy(UserContextHolder.getUserId());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);

        if (dto.getRoleCodes() != null && !dto.getRoleCodes().isEmpty()) {
            replaceUserRoles(userId, validateRoles(dto.getRoleCodes()), user.getUpdatedAt());
        }
        return loadUserProfile(userId);
    }

    @Transactional
    public void updateUserStatus(Long userId, UpdateUserStatusDto dto) {
        requireUser(userId);
        String status = parseUserStatus(dto.getStatus()).name();
        userMapper.updateStatus(userId, status, UserContextHolder.getUserId(), LocalDateTime.now());
    }

    @Transactional
    public UserProfileVO updateUserRoles(Long userId, UpdateUserRolesDto dto) {
        UserEntity user = requireUser(userId);
        List<RoleEntity> roles = validateRoles(dto.getRoleCodes());
        replaceUserRoles(userId, roles, LocalDateTime.now());
        return buildUserProfile(user, extractRoleCodes(roles));
    }

    private UserProfileVO loadUserProfile(Long userId) {
        UserEntity user = requireUser(userId);
        return buildUserProfile(user, userRoleMapper.selectRoleCodesByUserId(userId));
    }

    private UserEntity requireUser(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }
        return user;
    }

    private List<RoleEntity> validateRoles(List<String> roleCodes) {
        List<RoleEntity> roles = roleMapper.selectByRoleCodes(roleCodes);
        if (roles.size() != roleCodes.size()) {
            throw new IllegalArgumentException("invalid role code exists");
        }
        return roles;
    }

    private void replaceUserRoles(Long userId, List<RoleEntity> roles, LocalDateTime now) {
        userRoleMapper.deleteByUserId(userId);
        for (RoleEntity role : roles) {
            UserRoleEntity userRole = new UserRoleEntity();
            userRole.setId(IdWorker.getId());
            userRole.setUserId(userId);
            userRole.setRoleId(role.getId());
            userRole.setCreatedAt(now);
            userRoleMapper.insert(userRole);
        }
    }

    private List<String> extractRoleCodes(List<RoleEntity> roles) {
        return roles.stream().map(RoleEntity::getRoleCode).toList();
    }

    private UserStatusEnum parseUserStatus(String status) {
        try {
            return UserStatusEnum.valueOf(status);
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid user status");
        }
    }

    private UserProfileVO buildUserProfile(UserEntity user, List<String> roleCodes) {
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setStatus(user.getStatus());
        vo.setRoles(roleCodes);
        return vo;
    }
}
