package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.auth.AuthUser;
import com.manage.managesystem.auth.PasswordUtils;
import com.manage.managesystem.auth.TokenService;
import com.manage.managesystem.auth.TokenSession;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.dto.LoginDto;
import com.manage.managesystem.dto.RegisterDto;
import com.manage.managesystem.entity.RoleEntity;
import com.manage.managesystem.entity.UserEntity;
import com.manage.managesystem.entity.UserRoleEntity;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.enums.UserStatusEnum;
import com.manage.managesystem.mapper.RoleMapper;
import com.manage.managesystem.mapper.UserMapper;
import com.manage.managesystem.mapper.UserRoleMapper;
import com.manage.managesystem.vo.LoginVO;
import com.manage.managesystem.vo.UserProfileVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final TokenService tokenService;

    public AuthService(UserMapper userMapper, RoleMapper roleMapper, UserRoleMapper userRoleMapper, TokenService tokenService) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.tokenService = tokenService;
    }

    @Transactional
    public UserProfileVO register(RegisterDto dto) {
        UserEntity exists = userMapper.selectByUsername(dto.getUsername());
        if (exists != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        RoleEntity defaultRole = roleMapper.selectByRoleCode(SystemRoleEnum.TEAM_MEMBER.name());
        if (defaultRole == null) {
            throw new IllegalArgumentException("默认角色不存在，请先初始化角色数据");
        }
        LocalDateTime now = LocalDateTime.now();
        UserEntity user = new UserEntity();
        user.setId(IdWorker.getId());
        user.setUsername(dto.getUsername());
        user.setPasswordHash(PasswordUtils.hash(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(UserStatusEnum.ACTIVE.name());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setDeleted(0);
        userMapper.insert(user);

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setId(IdWorker.getId());
        userRole.setUserId(user.getId());
        userRole.setRoleId(defaultRole.getId());
        userRole.setCreatedAt(now);
        userRoleMapper.insert(userRole);
        return buildUserProfile(user, List.of(defaultRole.getRoleCode()));
    }

    @Transactional
    public LoginVO login(LoginDto dto) {
        UserEntity user = userMapper.selectByUsername(dto.getUsername());
        if (user == null || user.getDeleted() != null && user.getDeleted() == 1) {
            throw new IllegalArgumentException("账号不存在");
        }
        if (!UserStatusEnum.ACTIVE.name().equals(user.getStatus())) {
            throw new IllegalArgumentException("账号已停用");
        }
        if (!PasswordUtils.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("密码错误");
        }
        List<String> roleCodes = userRoleMapper.selectRoleCodesByUserId(user.getId());
        TokenSession session = tokenService.createSession(TokenSession.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .roleCodes(roleCodes)
                .build());
        LocalDateTime now = LocalDateTime.now();
        userMapper.updateLastLogin(user.getId(), now, now);
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(session.getToken());
        loginVO.setUser(buildUserProfile(user, roleCodes));
        loginVO.setExpiresAt(session.getExpiresAt());
        return loginVO;
    }

    public void logout(String token) {
        tokenService.invalidate(token);
    }

    public UserProfileVO currentUser() {
        AuthUser authUser = UserContextHolder.get();
        if (authUser == null) {
            throw new IllegalArgumentException("当前用户未登录");
        }
        UserEntity user = userMapper.selectById(authUser.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("当前用户不存在");
        }
        List<String> roleCodes = userRoleMapper.selectRoleCodesByUserId(user.getId());
        return buildUserProfile(user, roleCodes);
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
