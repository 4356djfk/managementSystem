package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.dto.AddProjectMemberDto;
import com.manage.managesystem.dto.UpdateProjectMemberDto;
import com.manage.managesystem.entity.ProjectMemberEntity;
import com.manage.managesystem.enums.ProjectMemberStatusEnum;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.mapper.ProjectMemberMapper;
import com.manage.managesystem.vo.ProjectMemberVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectMemberService {
    private final ProjectMemberMapper projectMemberMapper;

    public ProjectMemberService(ProjectMemberMapper projectMemberMapper) {
        this.projectMemberMapper = projectMemberMapper;
    }

    public List<ProjectMemberVO> list(Long projectId) {
        return projectMemberMapper.selectByProjectId(projectId);
    }

    @Transactional
    public ProjectMemberVO add(Long projectId, AddProjectMemberDto dto) {
        LocalDateTime now = LocalDateTime.now();
        ProjectMemberEntity entity = new ProjectMemberEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setUserId(dto.getUserId());
        entity.setProjectRole(dto.getProjectRole());
        entity.setMemberStatus(ProjectMemberStatusEnum.ACTIVE.name());
        entity.setJoinedAt(now);
        entity.setCreatedBy(UserContextHolder.getUserId());
        entity.setCreatedAt(now);
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(now);
        projectMemberMapper.insert(entity);
        return projectMemberMapper.selectById(entity.getId());
    }

    @Transactional
    public ProjectMemberVO update(Long projectId, Long memberId, UpdateProjectMemberDto dto) {
        ProjectMemberEntity entity = ensureMember(projectId, memberId);
        String nextRole = dto.getProjectRole();
        String nextStatus = dto.getMemberStatus() == null ? entity.getMemberStatus() : dto.getMemberStatus();

        if (isOnlyActiveManager(projectId, entity) && (!SystemRoleEnum.PROJECT_MANAGER.name().equals(nextRole)
                || ProjectMemberStatusEnum.REMOVED.name().equals(nextStatus))) {
            throw new IllegalArgumentException("项目至少需要保留一名激活状态的项目经理");
        }

        entity.setProjectRole(nextRole);
        entity.setMemberStatus(nextStatus);
        entity.setLeftAt(ProjectMemberStatusEnum.REMOVED.name().equals(nextStatus) ? LocalDateTime.now() : null);
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(LocalDateTime.now());
        projectMemberMapper.update(entity);
        return projectMemberMapper.selectById(memberId);
    }

    @Transactional
    public void remove(Long projectId, Long memberId) {
        ProjectMemberEntity entity = ensureMember(projectId, memberId);
        if (isOnlyActiveManager(projectId, entity)) {
            throw new IllegalArgumentException("项目至少需要保留一名激活状态的项目经理");
        }
        entity.setMemberStatus(ProjectMemberStatusEnum.REMOVED.name());
        entity.setLeftAt(LocalDateTime.now());
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(LocalDateTime.now());
        projectMemberMapper.update(entity);
    }

    private ProjectMemberEntity ensureMember(Long projectId, Long memberId) {
        ProjectMemberEntity entity = projectMemberMapper.selectEntityById(memberId);
        if (entity == null || !projectId.equals(entity.getProjectId())) {
            throw new IllegalArgumentException("项目成员不存在");
        }
        return entity;
    }

    private boolean isOnlyActiveManager(Long projectId, ProjectMemberEntity entity) {
        return SystemRoleEnum.PROJECT_MANAGER.name().equals(entity.getProjectRole())
                && ProjectMemberStatusEnum.ACTIVE.name().equals(entity.getMemberStatus())
                && projectMemberMapper.countActiveManagers(projectId) <= 1;
    }
}
