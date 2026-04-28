package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.CreateRiskDto;
import com.manage.managesystem.dto.RiskQueryDto;
import com.manage.managesystem.dto.UpdateRiskDto;
import com.manage.managesystem.dto.UpdateRiskStatusDto;
import com.manage.managesystem.entity.RiskEntity;
import com.manage.managesystem.enums.RiskStatusEnum;
import com.manage.managesystem.mapper.RiskMapper;
import com.manage.managesystem.mapper.TaskMapper;
import com.manage.managesystem.vo.RiskMatrixVO;
import com.manage.managesystem.vo.RiskVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class RiskService {
    private final RiskMapper riskMapper;
    private final TaskMapper taskMapper;
    private final ProjectPermissionService projectPermissionService;
    private final NotificationService notificationService;

    public RiskService(RiskMapper riskMapper,
                       TaskMapper taskMapper,
                       ProjectPermissionService projectPermissionService,
                       NotificationService notificationService) {
        this.riskMapper = riskMapper;
        this.taskMapper = taskMapper;
        this.projectPermissionService = projectPermissionService;
        this.notificationService = notificationService;
    }

    public PageResult<RiskVO> list(Long projectId, RiskQueryDto queryDto) {
        projectPermissionService.ensureProjectParticipant(projectId);
        List<RiskVO> list = riskMapper.selectByProjectId(projectId, queryDto);
        PageResult<RiskVO> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setPage(queryDto.getPage() == null ? 1 : queryDto.getPage());
        pageResult.setPageSize(queryDto.getPageSize() == null ? list.size() : queryDto.getPageSize());
        pageResult.setTotal((long) list.size());
        return pageResult;
    }

    @Transactional
    public RiskVO create(Long projectId, CreateRiskDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        LocalDateTime now = LocalDateTime.now();
        RiskEntity entity = new RiskEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setRiskCode("RISK" + entity.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setProbability(dto.getProbability());
        entity.setImpact(dto.getImpact());
        entity.setLevel(dto.getLevel());
        entity.setStatus(RiskStatusEnum.IDENTIFIED.name());
        entity.setResponseStrategy(dto.getResponseStrategy());
        entity.setTaskId(normalizeTaskId(projectId, dto.getTaskId()));
        entity.setPhaseName(normalizePhaseName(dto.getPhaseName()));
        entity.setOwnerId(dto.getOwnerId());
        entity.setIdentifiedAt(now);
        entity.setCreatedBy(UserContextHolder.getUserId());
        entity.setCreatedAt(now);
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        riskMapper.insert(entity);
        notificationService.notifyRiskAssigned(entity.getOwnerId(), projectId, entity.getId(), entity.getName(), entity.getLevel());
        return riskMapper.selectById(entity.getId());
    }

    @Transactional
    public RiskVO update(Long projectId, Long id, UpdateRiskDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        RiskEntity entity = ensureRisk(projectId, id);
        Long originalOwnerId = entity.getOwnerId();
        String originalLevel = entity.getLevel();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setProbability(dto.getProbability());
        entity.setImpact(dto.getImpact());
        entity.setLevel(dto.getLevel());
        entity.setResponseStrategy(dto.getResponseStrategy());
        entity.setTaskId(normalizeTaskId(projectId, dto.getTaskId()));
        entity.setPhaseName(normalizePhaseName(dto.getPhaseName()));
        entity.setOwnerId(dto.getOwnerId());
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(LocalDateTime.now());
        riskMapper.update(entity);
        if (!Objects.equals(originalOwnerId, entity.getOwnerId())) {
            notificationService.notifyRiskAssigned(entity.getOwnerId(), projectId, entity.getId(), entity.getName(), entity.getLevel());
        } else if (!Objects.equals(originalLevel, entity.getLevel()) && entity.getOwnerId() != null) {
            notificationService.notifyRiskUpdated(entity.getOwnerId(), projectId, entity.getId(), entity.getName(), entity.getLevel(), entity.getStatus());
        }
        return riskMapper.selectById(id);
    }

    @Transactional
    public RiskVO updateStatus(Long projectId, Long id, UpdateRiskStatusDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        RiskEntity entity = ensureRisk(projectId, id);
        String status = normalizeStatus(dto.getStatus());
        LocalDateTime closedAt = RiskStatusEnum.CLOSED.name().equals(status) ? LocalDateTime.now() : null;
        riskMapper.updateStatus(id, status, closedAt, UserContextHolder.getUserId(), LocalDateTime.now());
        if (!Objects.equals(entity.getStatus(), status)) {
            notificationService.notifyRiskUpdated(entity.getOwnerId(), projectId, entity.getId(), entity.getName(), entity.getLevel(), status);
        }
        return riskMapper.selectById(entity.getId());
    }

    @Transactional
    public void delete(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureRisk(projectId, id);
        riskMapper.softDelete(id, projectId, UserContextHolder.getUserId(), LocalDateTime.now());
    }

    public RiskMatrixVO riskMatrix(Long projectId) {
        projectPermissionService.ensureProjectParticipant(projectId);
        var levels = riskMapper.selectRiskMatrix(projectId);
        RiskMatrixVO vo = new RiskMatrixVO();
        vo.setLevels(levels);
        vo.setHighCount((int) levels.stream().filter(item -> "HIGH".equals(item.getLevel())).count());
        vo.setCriticalCount((int) levels.stream().filter(item -> "CRITICAL".equals(item.getLevel())).count());
        return vo;
    }

    private RiskEntity ensureRisk(Long projectId, Long id) {
        RiskEntity entity = riskMapper.selectEntityById(id);
        if (entity == null || !projectId.equals(entity.getProjectId())) {
            throw new IllegalArgumentException("risk not found");
        }
        return entity;
    }

    private String normalizeStatus(String status) {
        try {
            return RiskStatusEnum.valueOf(status).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid risk status");
        }
    }

    private Long normalizeTaskId(Long projectId, Long taskId) {
        if (taskId == null) {
            return null;
        }
        if (taskMapper.selectEntityById(projectId, taskId) == null) {
            throw new IllegalArgumentException("risk task not found");
        }
        return taskId;
    }

    private String normalizePhaseName(String phaseName) {
        if (phaseName == null) {
            return null;
        }
        String value = phaseName.trim();
        return value.isEmpty() ? null : value;
    }
}
