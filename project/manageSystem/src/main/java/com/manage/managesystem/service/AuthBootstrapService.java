package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.auth.PasswordUtils;
import com.manage.managesystem.entity.RoleEntity;
import com.manage.managesystem.entity.UserEntity;
import com.manage.managesystem.entity.UserRoleEntity;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.enums.UserStatusEnum;
import com.manage.managesystem.mapper.RoleMapper;
import com.manage.managesystem.mapper.UserMapper;
import com.manage.managesystem.mapper.UserRoleMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class AuthBootstrapService implements CommandLineRunner {
    private final RoleMapper roleMapper;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    public AuthBootstrapService(RoleMapper roleMapper, UserMapper userMapper, UserRoleMapper userRoleMapper) {
        this.roleMapper = roleMapper;
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public void run(String... args) {
        initRoles();
        initAdmin();
    }

    private void initRoles() {
        Map<SystemRoleEnum, String> roleNames = Map.of(
                SystemRoleEnum.SYS_ADMIN, "系统管理员",
                SystemRoleEnum.USER, "产品用户"
        );
        for (Map.Entry<SystemRoleEnum, String> entry : roleNames.entrySet()) {
            if (roleMapper.selectByRoleCode(entry.getKey().name()) != null) {
                continue;
            }
            LocalDateTime now = LocalDateTime.now();
            RoleEntity role = new RoleEntity();
            role.setId(IdWorker.getId());
            role.setRoleCode(entry.getKey().name());
            role.setRoleName(entry.getValue());
            role.setDescription(entry.getValue());
            role.setCreatedAt(now);
            role.setUpdatedAt(now);
            roleMapper.insert(role);
        }
    }

    private void initAdmin() {
        LocalDateTime now = LocalDateTime.now();
        UserEntity adminUser = userMapper.selectByUsername("admin");

        if (adminUser != null) {
            adminUser.setPasswordHash(PasswordUtils.hash("123456"));
            adminUser.setRealName("系统管理员");
            adminUser.setStatus(UserStatusEnum.ACTIVE.name());
            adminUser.setUpdatedAt(now);
            userMapper.update(adminUser);
            return;
        }

        RoleEntity adminRole = roleMapper.selectByRoleCode(SystemRoleEnum.SYS_ADMIN.name());
        if (adminRole == null) {
            return;
        }

        UserEntity user = new UserEntity();
        user.setId(IdWorker.getId());
        user.setUsername("admin");
        user.setPasswordHash(PasswordUtils.hash("123456"));
        user.setRealName("系统管理员");
        user.setStatus(UserStatusEnum.ACTIVE.name());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setDeleted(0);
        userMapper.insert(user);

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setId(IdWorker.getId());
        userRole.setUserId(user.getId());
        userRole.setRoleId(adminRole.getId());
        userRole.setCreatedAt(now);
        userRoleMapper.insert(userRole);
    }
}
