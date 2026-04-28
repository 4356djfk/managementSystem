package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.CreateQualityActivityDto;
import com.manage.managesystem.dto.CreateQualityMetricDto;
import com.manage.managesystem.dto.CreateQualityPlanDto;
import com.manage.managesystem.dto.QualityActivityQueryDto;
import com.manage.managesystem.dto.QualityMetricQueryDto;
import com.manage.managesystem.dto.QualityPlanQueryDto;
import com.manage.managesystem.dto.UpdateQualityActivityDto;
import com.manage.managesystem.dto.UpdateQualityMetricDto;
import com.manage.managesystem.dto.UpdateQualityPlanDto;
import com.manage.managesystem.entity.QualityActivityEntity;
import com.manage.managesystem.entity.QualityMetricEntity;
import com.manage.managesystem.entity.QualityPlanEntity;
import com.manage.managesystem.enums.QualityActivityTypeEnum;
import com.manage.managesystem.enums.QualityPlanStatusEnum;
import com.manage.managesystem.mapper.ProjectMapper;
import com.manage.managesystem.mapper.QualityMapper;
import com.manage.managesystem.mapper.UserMapper;
import com.manage.managesystem.vo.QualityActivityVO;
import com.manage.managesystem.vo.QualityMetricVO;
import com.manage.managesystem.vo.QualityPlanVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QualityService {
    private final QualityMapper qualityMapper;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;
    private final ProjectPermissionService projectPermissionService;

    public QualityService(QualityMapper qualityMapper,
                          ProjectMapper projectMapper,
                          UserMapper userMapper,
                          ProjectPermissionService projectPermissionService) {
        this.qualityMapper = qualityMapper;
        this.projectMapper = projectMapper;
        this.userMapper = userMapper;
        this.projectPermissionService = projectPermissionService;
    }

    public PageResult<QualityPlanVO> listPlans(Long projectId, QualityPlanQueryDto queryDto) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProjectExists(projectId);
        List<QualityPlanVO> list = qualityMapper.selectPlansByProjectId(projectId, queryDto);
        return buildPageResult(list, queryDto.getPage(), queryDto.getPageSize());
    }

    @Transactional
    public QualityPlanVO createPlan(Long projectId, CreateQualityPlanDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);
        validateOwner(dto.getOwnerId());
        LocalDateTime now = LocalDateTime.now();

        QualityPlanEntity entity = new QualityPlanEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setTitle(dto.getTitle().trim());
        entity.setQualityStandard(normalizeText(dto.getQualityStandard()));
        entity.setAcceptanceRule(normalizeText(dto.getAcceptanceRule()));
        entity.setOwnerId(dto.getOwnerId());
        entity.setStatus(normalizePlanStatus(dto.getStatus()));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        qualityMapper.insertPlan(entity);
        return requirePlan(projectId, entity.getId());
    }

    @Transactional
    public QualityPlanVO updatePlan(Long projectId, Long id, UpdateQualityPlanDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);
        validateOwner(dto.getOwnerId());
        QualityPlanEntity entity = requirePlanEntity(projectId, id);
        entity.setTitle(dto.getTitle().trim());
        entity.setQualityStandard(normalizeText(dto.getQualityStandard()));
        entity.setAcceptanceRule(normalizeText(dto.getAcceptanceRule()));
        entity.setOwnerId(dto.getOwnerId());
        entity.setStatus(dto.getStatus() == null || dto.getStatus().isBlank()
                ? entity.getStatus()
                : normalizePlanStatus(dto.getStatus()));
        entity.setUpdatedAt(LocalDateTime.now());
        qualityMapper.updatePlan(entity);
        return requirePlan(projectId, id);
    }

    @Transactional
    public void deletePlan(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);
        requirePlanEntity(projectId, id);
        if (qualityMapper.countActivitiesByPlanId(projectId, id) > 0) {
            throw new IllegalArgumentException("quality plan still has activities");
        }
        qualityMapper.softDeletePlan(projectId, id);
    }

    public PageResult<QualityActivityVO> listActivities(Long projectId, QualityActivityQueryDto queryDto) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProjectExists(projectId);
        List<QualityActivityVO> list = qualityMapper.selectActivitiesByProjectId(projectId, queryDto);
        return buildPageResult(list, queryDto.getPage(), queryDto.getPageSize());
    }

    @Transactional
    public QualityActivityVO createActivity(Long projectId, CreateQualityActivityDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);
        validatePlan(projectId, dto.getQualityPlanId());
        validateOwner(dto.getOwnerId());
        LocalDateTime now = LocalDateTime.now();

        QualityActivityEntity entity = new QualityActivityEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setQualityPlanId(dto.getQualityPlanId());
        entity.setActivityName(dto.getActivityName().trim());
        entity.setActivityType(normalizeActivityType(dto.getActivityType()));
        entity.setPlannedDate(dto.getPlannedDate());
        entity.setActualDate(dto.getActualDate());
        entity.setResult(normalizeText(dto.getResult()));
        entity.setOwnerId(dto.getOwnerId());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        qualityMapper.insertActivity(entity);
        return requireActivity(projectId, entity.getId());
    }

    @Transactional
    public QualityActivityVO updateActivity(Long projectId, Long id, UpdateQualityActivityDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);
        validatePlan(projectId, dto.getQualityPlanId());
        validateOwner(dto.getOwnerId());
        QualityActivityEntity entity = requireActivityEntity(projectId, id);
        entity.setQualityPlanId(dto.getQualityPlanId());
        entity.setActivityName(dto.getActivityName().trim());
        entity.setActivityType(normalizeActivityType(dto.getActivityType()));
        entity.setPlannedDate(dto.getPlannedDate());
        entity.setActualDate(dto.getActualDate());
        entity.setResult(normalizeText(dto.getResult()));
        entity.setOwnerId(dto.getOwnerId());
        entity.setUpdatedAt(LocalDateTime.now());
        qualityMapper.updateActivity(entity);
        return requireActivity(projectId, id);
    }

    @Transactional
    public void deleteActivity(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);
        requireActivityEntity(projectId, id);
        qualityMapper.softDeleteActivity(projectId, id);
    }

    public PageResult<QualityMetricVO> listMetrics(Long projectId, QualityMetricQueryDto queryDto) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProjectExists(projectId);
        List<QualityMetricVO> list = qualityMapper.selectMetricsByProjectId(projectId, queryDto);
        return buildPageResult(list, queryDto.getPage(), queryDto.getPageSize());
    }

    @Transactional
    public QualityMetricVO createMetric(Long projectId, CreateQualityMetricDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);
        LocalDateTime now = LocalDateTime.now();

        QualityMetricEntity entity = new QualityMetricEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setMetricName(dto.getMetricName().trim());
        entity.setMetricValue(dto.getMetricValue());
        entity.setMetricUnit(normalizeText(dto.getMetricUnit()));
        entity.setStatisticDate(dto.getStatisticDate());
        entity.setCreatedAt(now);
        qualityMapper.insertMetric(entity);
        return requireMetric(projectId, entity.getId());
    }

    @Transactional
    public QualityMetricVO updateMetric(Long projectId, Long id, UpdateQualityMetricDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);
        QualityMetricEntity entity = requireMetricEntity(projectId, id);
        entity.setMetricName(dto.getMetricName().trim());
        entity.setMetricValue(dto.getMetricValue());
        entity.setMetricUnit(normalizeText(dto.getMetricUnit()));
        entity.setStatisticDate(dto.getStatisticDate());
        qualityMapper.updateMetric(entity);
        return requireMetric(projectId, id);
    }

    @Transactional
    public void deleteMetric(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);
        requireMetricEntity(projectId, id);
        qualityMapper.deleteMetric(projectId, id);
    }

    private void ensureProjectExists(Long projectId) {
        if (projectMapper.selectEntityById(projectId) == null) {
            throw new IllegalArgumentException("project not found");
        }
    }

    private void validateOwner(Long ownerId) {
        if (ownerId != null && userMapper.selectById(ownerId) == null) {
            throw new IllegalArgumentException("quality owner not found");
        }
    }

    private void validatePlan(Long projectId, Long qualityPlanId) {
        if (qualityPlanId != null && qualityMapper.selectPlanEntityById(projectId, qualityPlanId) == null) {
            throw new IllegalArgumentException("quality plan not found");
        }
    }

    private String normalizePlanStatus(String status) {
        if (status == null || status.isBlank()) {
            return QualityPlanStatusEnum.DRAFT.name();
        }
        try {
            return QualityPlanStatusEnum.valueOf(status.trim().toUpperCase()).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid quality plan status");
        }
    }

    private String normalizeActivityType(String activityType) {
        try {
            return QualityActivityTypeEnum.valueOf(activityType.trim().toUpperCase()).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid quality activity type");
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private QualityPlanVO requirePlan(Long projectId, Long id) {
        QualityPlanVO vo = qualityMapper.selectPlanById(projectId, id);
        if (vo == null) {
            throw new IllegalArgumentException("quality plan not found");
        }
        return vo;
    }

    private QualityPlanEntity requirePlanEntity(Long projectId, Long id) {
        QualityPlanEntity entity = qualityMapper.selectPlanEntityById(projectId, id);
        if (entity == null) {
            throw new IllegalArgumentException("quality plan not found");
        }
        return entity;
    }

    private QualityActivityVO requireActivity(Long projectId, Long id) {
        QualityActivityVO vo = qualityMapper.selectActivityById(projectId, id);
        if (vo == null) {
            throw new IllegalArgumentException("quality activity not found");
        }
        return vo;
    }

    private QualityActivityEntity requireActivityEntity(Long projectId, Long id) {
        QualityActivityEntity entity = qualityMapper.selectActivityEntityById(projectId, id);
        if (entity == null) {
            throw new IllegalArgumentException("quality activity not found");
        }
        return entity;
    }

    private QualityMetricVO requireMetric(Long projectId, Long id) {
        QualityMetricVO vo = qualityMapper.selectMetricById(projectId, id);
        if (vo == null) {
            throw new IllegalArgumentException("quality metric not found");
        }
        return vo;
    }

    private QualityMetricEntity requireMetricEntity(Long projectId, Long id) {
        QualityMetricEntity entity = qualityMapper.selectMetricEntityById(projectId, id);
        if (entity == null) {
            throw new IllegalArgumentException("quality metric not found");
        }
        return entity;
    }

    private <T> PageResult<T> buildPageResult(List<T> list, Integer page, Integer pageSize) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setPage(page == null ? 1 : page);
        pageResult.setPageSize(pageSize == null ? list.size() : pageSize);
        pageResult.setTotal((long) list.size());
        return pageResult;
    }
}
