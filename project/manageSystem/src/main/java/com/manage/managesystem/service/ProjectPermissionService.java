package com.manage.managesystem.service;

import com.manage.managesystem.auth.AuthUser;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.entity.ProjectEntity;
import com.manage.managesystem.enums.ProjectRoleEnum;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.mapper.ProjectMapper;
import com.manage.managesystem.mapper.ProjectMemberMapper;
import com.manage.managesystem.mapper.UserRoleMapper;
import com.manage.managesystem.util.RoleCodeUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ProjectPermissionService {
    public static final String PROJECT_ROLE_OWNER = ProjectRoleEnum.OWNER.name();
    private static final String LEGACY_ROLE_PARTICIPANT = "PARTICIPANT";
    private static final String LEGACY_ROLE_PROJECT_MANAGER = "PROJECT_MANAGER";
    private static final Set<String> EDITABLE_MEMBER_ROLES = Set.of(ProjectRoleEnum.TEAM_MEMBER.name());

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final UserRoleMapper userRoleMapper;

    public ProjectPermissionService(ProjectMapper projectMapper,
                                    ProjectMemberMapper projectMemberMapper,
                                    UserRoleMapper userRoleMapper) {
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.userRoleMapper = userRoleMapper;
    }

    public Long requireCurrentUserId() {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("user not authenticated");
        }
        return userId;
    }

    public boolean isSysAdmin() {
        AuthUser authUser = UserContextHolder.get();
        if (authUser == null) {
            return false;
        }
        List<String> roleCodes = authUser.getRoleCodes();
        return roleCodes != null && roleCodes.contains(SystemRoleEnum.SYS_ADMIN.name());
    }

    public void ensureSysAdmin() {
        if (!isSysAdmin()) {
            throw new IllegalArgumentException("no permission to access system feature");
        }
    }

    public boolean hasCurrentUserBusinessRole() {
        AuthUser authUser = UserContextHolder.get();
        if (authUser == null) {
            return false;
        }
        return isProjectBusinessRoleSet(authUser.getRoleCodes());
    }

    public void ensureCurrentUserHasBusinessRole() {
        if (!hasCurrentUserBusinessRole()) {
            throw new IllegalArgumentException("current user is not allowed to participate in project business");
        }
    }

    public boolean hasBusinessRole(Long userId) {
        if (userId == null) {
            return false;
        }
        List<String> roleCodes = RoleCodeUtils.normalizeSystemRoles(userRoleMapper.selectRoleCodesByUserId(userId));
        return isProjectBusinessRoleSet(roleCodes);
    }

    public void ensureBusinessUser(Long userId, String message) {
        if (!hasBusinessRole(userId)) {
            throw new IllegalArgumentException(message);
        }
    }

    public ProjectEntity requireProject(Long projectId) {
        ProjectEntity project = projectMapper.selectEntityById(projectId);
        if (project == null) {
            throw new IllegalArgumentException("project not found");
        }
        return project;
    }

    public boolean isProjectOwner(Long projectId) {
        return isProjectOwner(requireProject(projectId));
    }

    public boolean isProjectOwner(ProjectEntity project) {
        Long userId = requireCurrentUserId();
        return project != null
                && hasCurrentUserBusinessRole()
                && userId.equals(project.getOwnerId());
    }

    public boolean isProjectParticipant(Long projectId) {
        return canAccessProject(projectId, requireCurrentUserId());
    }

    public boolean isProjectEditor(Long projectId) {
        String roleCode = getCurrentProjectRole(projectId);
        return roleCode != null && !ProjectRoleEnum.READ_ONLY.name().equals(roleCode);
    }

    public boolean isProjectParticipant(ProjectEntity project) {
        if (project == null) {
            return false;
        }
        Long userId = requireCurrentUserId();
        return hasCurrentUserBusinessRole()
                && (userId.equals(project.getOwnerId()) || loadActiveMemberRole(project.getId(), userId) != null);
    }

    public boolean isActiveParticipant(Long projectId, Long userId) {
        if (userId == null) {
            return false;
        }
        return loadActiveMemberRole(projectId, userId) != null;
    }

    public boolean canAccessProject(Long projectId, Long userId) {
        if (projectId == null || userId == null || !hasBusinessRole(userId)) {
            return false;
        }
        ProjectEntity project = projectMapper.selectEntityById(projectId);
        if (project == null) {
            return false;
        }
        return userId.equals(project.getOwnerId()) || loadActiveMemberRole(projectId, userId) != null;
    }

    public boolean isAssignableProjectUser(Long projectId, Long userId) {
        if (userId == null || !hasBusinessRole(userId)) {
            return false;
        }
        ProjectEntity project = requireProject(projectId);
        if (project != null && userId.equals(project.getOwnerId())) {
            return true;
        }
        String roleCode = loadActiveMemberRole(projectId, userId);
        return roleCode != null && EDITABLE_MEMBER_ROLES.contains(roleCode);
    }

    public String getCurrentProjectRole(Long projectId) {
        ProjectEntity project = requireProject(projectId);
        if (isProjectOwner(project)) {
            return PROJECT_ROLE_OWNER;
        }
        return loadActiveMemberRole(projectId, requireCurrentUserId());
    }

    public void ensureProjectOwner(Long projectId) {
        ProjectEntity project = requireProject(projectId);
        if (!isProjectOwner(project)) {
            throw new IllegalArgumentException("no permission to manage this project");
        }
    }

    public void ensureProjectParticipant(Long projectId) {
        ProjectEntity project = requireProject(projectId);
        if (!isProjectParticipant(project)) {
            throw new IllegalArgumentException("no permission to access this project");
        }
    }

    public void ensureProjectEditor(Long projectId) {
        if (!isProjectEditor(projectId)) {
            throw new IllegalArgumentException("no permission to edit this project");
        }
    }

    public String normalizeMemberRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return ProjectRoleEnum.TEAM_MEMBER.name();
        }
        String normalized = roleCode.trim().toUpperCase();
        if (ProjectRoleEnum.READ_ONLY.name().equals(normalized)) {
            return ProjectRoleEnum.READ_ONLY.name();
        }
        if (ProjectRoleEnum.TEAM_MEMBER.name().equals(normalized)
                || LEGACY_ROLE_PARTICIPANT.equals(normalized)
                || LEGACY_ROLE_PROJECT_MANAGER.equals(normalized)) {
            return ProjectRoleEnum.TEAM_MEMBER.name();
        }
        throw new IllegalArgumentException("invalid project role");
    }

    public String defaultMemberRole() {
        return ProjectRoleEnum.READ_ONLY.name();
    }

    private String loadActiveMemberRole(Long projectId, Long userId) {
        if (!hasBusinessRole(userId)) {
            return null;
        }
        String roleCode = projectMemberMapper.selectActiveRoleByProjectAndUser(projectId, userId);
        if (roleCode == null || roleCode.isBlank()) {
            return null;
        }
        return normalizeMemberRole(roleCode);
    }

    private boolean isProjectBusinessRoleSet(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return false;
        }
        return roleCodes.contains(SystemRoleEnum.USER.name())
                && !roleCodes.contains(SystemRoleEnum.SYS_ADMIN.name());
    }
}
