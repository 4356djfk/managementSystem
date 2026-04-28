package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.dto.AddProjectMemberDto;
import com.manage.managesystem.dto.UpdateProjectMemberDto;
import com.manage.managesystem.entity.ProjectMemberEntity;
import com.manage.managesystem.enums.ProjectMemberStatusEnum;
import com.manage.managesystem.mapper.ProjectMemberMapper;
import com.manage.managesystem.mapper.UserMapper;
import com.manage.managesystem.vo.ProjectMemberCandidateVO;
import com.manage.managesystem.vo.ProjectMemberVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ProjectMemberService {
    private final ProjectMemberMapper projectMemberMapper;
    private final UserMapper userMapper;
    private final ProjectPermissionService projectPermissionService;
    private final NotificationService notificationService;

    public ProjectMemberService(ProjectMemberMapper projectMemberMapper,
                                UserMapper userMapper,
                                ProjectPermissionService projectPermissionService,
                                NotificationService notificationService) {
        this.projectMemberMapper = projectMemberMapper;
        this.userMapper = userMapper;
        this.projectPermissionService = projectPermissionService;
        this.notificationService = notificationService;
    }

    public List<ProjectMemberVO> list(Long projectId) {
        projectPermissionService.ensureProjectParticipant(projectId);
        return projectMemberMapper.selectByProjectId(projectId).stream()
                .filter(member -> member.getUserId() != null && projectPermissionService.hasBusinessRole(member.getUserId()))
                .peek(this::normalizeMemberRole)
                .toList();
    }

    public List<ProjectMemberCandidateVO> listCandidates(Long projectId) {
        projectPermissionService.ensureProjectOwner(projectId);
        return userMapper.selectProjectMemberCandidates();
    }

    @Transactional
    public ProjectMemberVO add(Long projectId, AddProjectMemberDto dto) {
        projectPermissionService.ensureProjectOwner(projectId);
        projectPermissionService.ensureBusinessUser(dto.getUserId(), "selected user cannot join project");
        LocalDateTime now = LocalDateTime.now();
        ProjectMemberEntity entity = new ProjectMemberEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setUserId(dto.getUserId());
        entity.setProjectRole(resolveProjectRole(dto.getProjectRole(), true));
        entity.setMemberStatus(ProjectMemberStatusEnum.ACTIVE.name());
        entity.setJoinedAt(now);
        entity.setCreatedBy(UserContextHolder.getUserId());
        entity.setCreatedAt(now);
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(now);
        projectMemberMapper.insert(entity);
        notificationService.notifyProjectMemberAdded(entity.getUserId(), projectId, entity.getId(), entity.getProjectRole());
        ProjectMemberVO memberVO = projectMemberMapper.selectById(entity.getId());
        normalizeMemberRole(memberVO);
        return memberVO;
    }

    @Transactional
    public ProjectMemberVO update(Long projectId, Long memberId, UpdateProjectMemberDto dto) {
        projectPermissionService.ensureProjectOwner(projectId);
        ProjectMemberEntity entity = ensureMember(projectId, memberId);
        String originalRole = entity.getProjectRole();
        String originalStatus = entity.getMemberStatus();
        String nextStatus = resolveMemberStatus(dto.getMemberStatus(), entity.getMemberStatus());
        if (!ProjectMemberStatusEnum.REMOVED.name().equals(nextStatus)) {
            projectPermissionService.ensureBusinessUser(entity.getUserId(), "selected user cannot join project");
        }
        entity.setProjectRole(resolveProjectRole(dto.getProjectRole(), false, entity.getProjectRole()));
        entity.setMemberStatus(nextStatus);
        entity.setLeftAt(ProjectMemberStatusEnum.REMOVED.name().equals(nextStatus) ? LocalDateTime.now() : null);
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(LocalDateTime.now());
        projectMemberMapper.update(entity);
        if (!Objects.equals(originalRole, entity.getProjectRole())
                || !Objects.equals(originalStatus, entity.getMemberStatus())) {
            notificationService.notifyProjectMemberUpdated(
                    entity.getUserId(),
                    projectId,
                    entity.getId(),
                    entity.getProjectRole(),
                    entity.getMemberStatus()
            );
        }
        ProjectMemberVO memberVO = projectMemberMapper.selectById(memberId);
        normalizeMemberRole(memberVO);
        return memberVO;
    }

    @Transactional
    public void remove(Long projectId, Long memberId) {
        projectPermissionService.ensureProjectOwner(projectId);
        ProjectMemberEntity entity = ensureMember(projectId, memberId);
        entity.setMemberStatus(ProjectMemberStatusEnum.REMOVED.name());
        entity.setLeftAt(LocalDateTime.now());
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(LocalDateTime.now());
        projectMemberMapper.update(entity);
        notificationService.notifyProjectMemberUpdated(
                entity.getUserId(),
                projectId,
                entity.getId(),
                entity.getProjectRole(),
                entity.getMemberStatus()
        );
    }

    private ProjectMemberEntity ensureMember(Long projectId, Long memberId) {
        ProjectMemberEntity entity = projectMemberMapper.selectEntityById(memberId);
        if (entity == null || !projectId.equals(entity.getProjectId())) {
            throw new IllegalArgumentException("project member not found");
        }
        return entity;
    }

    private void normalizeMemberRole(ProjectMemberVO memberVO) {
        if (memberVO == null) {
            return;
        }
        memberVO.setProjectRole(projectPermissionService.normalizeMemberRole(memberVO.getProjectRole()));
    }

    private String resolveProjectRole(String projectRole, boolean useDefaultWhenBlank) {
        return resolveProjectRole(projectRole, useDefaultWhenBlank, null);
    }

    private String resolveProjectRole(String projectRole, boolean useDefaultWhenBlank, String currentRole) {
        if (projectRole == null || projectRole.isBlank()) {
            if (useDefaultWhenBlank) {
                return projectPermissionService.defaultMemberRole();
            }
            return projectPermissionService.normalizeMemberRole(currentRole);
        }
        return projectPermissionService.normalizeMemberRole(projectRole);
    }

    private String resolveMemberStatus(String memberStatus, String currentStatus) {
        if (memberStatus == null || memberStatus.isBlank()) {
            return currentStatus;
        }
        try {
            return ProjectMemberStatusEnum.valueOf(memberStatus.trim().toUpperCase()).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid project member status");
        }
    }
}
