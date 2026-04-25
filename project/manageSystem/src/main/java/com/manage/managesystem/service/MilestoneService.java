package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.dto.CreateMilestoneDto;
import com.manage.managesystem.dto.UpdateMilestoneDto;
import com.manage.managesystem.entity.MilestoneEntity;
import com.manage.managesystem.enums.MilestoneStatusEnum;
import com.manage.managesystem.mapper.MilestoneMapper;
import com.manage.managesystem.mapper.ProjectMapper;
import com.manage.managesystem.mapper.UserMapper;
import com.manage.managesystem.vo.MilestoneVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MilestoneService {
    private final MilestoneMapper milestoneMapper;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;

    public MilestoneService(MilestoneMapper milestoneMapper, ProjectMapper projectMapper, UserMapper userMapper) {
        this.milestoneMapper = milestoneMapper;
        this.projectMapper = projectMapper;
        this.userMapper = userMapper;
    }

    public List<MilestoneVO> list(Long projectId) {
        ensureProjectExists(projectId);
        return milestoneMapper.selectByProjectId(projectId);
    }

    @Transactional
    public MilestoneVO create(Long projectId, CreateMilestoneDto dto) {
        ensureProjectExists(projectId);
        validateOwner(dto.getOwnerId());
        LocalDateTime now = LocalDateTime.now();

        MilestoneEntity entity = new MilestoneEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPlannedDate(dto.getPlannedDate());
        entity.setStatus(MilestoneStatusEnum.PENDING.name());
        entity.setOwnerId(dto.getOwnerId());
        entity.setCreatedBy(UserContextHolder.getUserId());
        entity.setCreatedAt(now);
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        milestoneMapper.insert(entity);
        return requireMilestone(projectId, entity.getId());
    }

    @Transactional
    public MilestoneVO update(Long projectId, Long id, UpdateMilestoneDto dto) {
        ensureProjectExists(projectId);
        validateOwner(dto.getOwnerId());
        MilestoneEntity entity = requireMilestoneEntity(projectId, id);
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPlannedDate(dto.getPlannedDate());
        entity.setActualDate(dto.getActualDate());
        entity.setStatus(dto.getStatus() == null || dto.getStatus().isBlank()
                ? entity.getStatus()
                : parseStatus(dto.getStatus()).name());
        entity.setOwnerId(dto.getOwnerId());
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(LocalDateTime.now());
        milestoneMapper.update(entity);
        return requireMilestone(projectId, id);
    }

    @Transactional
    public void delete(Long projectId, Long id) {
        ensureProjectExists(projectId);
        requireMilestoneEntity(projectId, id);
        milestoneMapper.softDelete(projectId, id, UserContextHolder.getUserId(), LocalDateTime.now());
    }

    private MilestoneVO requireMilestone(Long projectId, Long id) {
        MilestoneVO milestone = milestoneMapper.selectById(projectId, id);
        if (milestone == null) {
            throw new IllegalArgumentException("milestone not found");
        }
        return milestone;
    }

    private MilestoneEntity requireMilestoneEntity(Long projectId, Long id) {
        MilestoneEntity milestone = milestoneMapper.selectEntityById(projectId, id);
        if (milestone == null) {
            throw new IllegalArgumentException("milestone not found");
        }
        return milestone;
    }

    private void ensureProjectExists(Long projectId) {
        if (projectMapper.selectEntityById(projectId) == null) {
            throw new IllegalArgumentException("project not found");
        }
    }

    private void validateOwner(Long ownerId) {
        if (ownerId != null && userMapper.selectById(ownerId) == null) {
            throw new IllegalArgumentException("milestone owner not found: " + ownerId);
        }
    }

    private MilestoneStatusEnum parseStatus(String status) {
        try {
            return MilestoneStatusEnum.valueOf(status);
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid milestone status");
        }
    }
}
