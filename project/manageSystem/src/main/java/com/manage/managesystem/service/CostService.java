package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.dto.CreateCostActualDto;
import com.manage.managesystem.dto.CreateCostBaselineDto;
import com.manage.managesystem.dto.CreateCostPlanDto;
import com.manage.managesystem.dto.UpdateCostPlanDto;
import com.manage.managesystem.entity.CostActualEntity;
import com.manage.managesystem.entity.CostPlanEntity;
import com.manage.managesystem.mapper.CostMapper;
import com.manage.managesystem.mapper.ProjectMapper;
import com.manage.managesystem.mapper.TaskMapper;
import com.manage.managesystem.vo.CostActualVO;
import com.manage.managesystem.vo.CostBaselineVO;
import com.manage.managesystem.vo.CostPlanVO;
import com.manage.managesystem.vo.EvmMetricVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CostService {
    private final CostMapper costMapper;
    private final ProjectMapper projectMapper;
    private final TaskMapper taskMapper;
    private final ObjectMapper objectMapper;

    public CostService(CostMapper costMapper, ProjectMapper projectMapper, TaskMapper taskMapper, ObjectMapper objectMapper) {
        this.costMapper = costMapper;
        this.projectMapper = projectMapper;
        this.taskMapper = taskMapper;
        this.objectMapper = objectMapper;
    }

    public List<CostPlanVO> listPlans(Long projectId) {
        ensureProjectExists(projectId);
        return costMapper.selectPlansByProjectId(projectId);
    }

    @Transactional
    public CostPlanVO createPlan(Long projectId, CreateCostPlanDto dto) {
        ensureProjectExists(projectId);
        validateTask(projectId, dto.getTaskId());
        LocalDateTime now = LocalDateTime.now();
        CostPlanEntity entity = new CostPlanEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setTaskId(dto.getTaskId());
        entity.setType(dto.getType());
        entity.setPhase(dto.getPhase());
        entity.setName(dto.getName());
        entity.setPlannedAmount(dto.getPlannedAmount());
        entity.setCurrency(dto.getCurrency());
        entity.setRemark(dto.getRemark());
        entity.setCreatedBy(UserContextHolder.getUserId());
        entity.setCreatedAt(now);
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        costMapper.insertPlan(entity);
        return requirePlan(projectId, entity.getId());
    }

    @Transactional
    public CostPlanVO updatePlan(Long projectId, Long id, UpdateCostPlanDto dto) {
        ensureProjectExists(projectId);
        validateTask(projectId, dto.getTaskId());
        CostPlanEntity entity = requirePlanEntity(projectId, id);
        entity.setTaskId(dto.getTaskId());
        entity.setType(dto.getType());
        entity.setPhase(dto.getPhase());
        entity.setName(dto.getName());
        entity.setPlannedAmount(dto.getPlannedAmount());
        entity.setCurrency(dto.getCurrency());
        entity.setRemark(dto.getRemark());
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(LocalDateTime.now());
        costMapper.updatePlan(entity);
        return requirePlan(projectId, id);
    }

    @Transactional
    public CostBaselineVO createBaseline(Long projectId, CreateCostBaselineDto dto) {
        ensureProjectExists(projectId);
        Long id = IdWorker.getId();
        LocalDateTime now = LocalDateTime.now();
        String snapshotJson = buildSnapshot(projectId, dto.getSnapshotJson());
        costMapper.insertBaseline(id, projectId, "COST", "V" + (costMapper.countBaselines(projectId, "COST") + 1),
                dto.getBaselineName(), snapshotJson, "PUBLISHED", UserContextHolder.getUserId(), now, now);
        return costMapper.selectBaselineById(projectId, id);
    }

    public List<CostActualVO> listActuals(Long projectId) {
        ensureProjectExists(projectId);
        return costMapper.selectActualsByProjectId(projectId);
    }

    public List<CostBaselineVO> listBaselines(Long projectId) {
        ensureProjectExists(projectId);
        return costMapper.selectBaselinesByProjectId(projectId, "COST");
    }

    @Transactional
    public void deletePlan(Long projectId, Long id) {
        ensureProjectExists(projectId);
        requirePlanEntity(projectId, id);
        costMapper.softDeletePlan(projectId, id, UserContextHolder.getUserId(), LocalDateTime.now());
    }

    @Transactional
    public CostActualVO createActual(Long projectId, CreateCostActualDto dto) {
        ensureProjectExists(projectId);
        validateTask(projectId, dto.getTaskId());
        LocalDateTime now = LocalDateTime.now();
        CostActualEntity entity = new CostActualEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setTaskId(dto.getTaskId());
        entity.setSourceType(dto.getSourceType());
        entity.setActualAmount(dto.getAmount());
        entity.setOccurredDate(dto.getSpendDate());
        entity.setDescription(dto.getRemark());
        entity.setRecorderId(UserContextHolder.getUserId());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        costMapper.insertActual(entity);
        return costMapper.selectActualById(projectId, entity.getId());
    }

    @Transactional
    public void deleteActual(Long projectId, Long id) {
        ensureProjectExists(projectId);
        requireActualEntity(projectId, id);
        costMapper.softDeleteActual(projectId, id, LocalDateTime.now());
    }

    @Transactional
    public void deleteBaseline(Long projectId, Long id) {
        ensureProjectExists(projectId);
        CostBaselineVO baseline = costMapper.selectBaselineById(projectId, id);
        if (baseline == null) {
            throw new IllegalArgumentException("cost baseline not found");
        }
        costMapper.deleteBaseline(projectId, id);
    }

    public EvmMetricVO evm(Long projectId) {
        ensureProjectExists(projectId);
        return costMapper.selectEvmMetric(projectId);
    }

    private void ensureProjectExists(Long projectId) {
        if (projectMapper.selectEntityById(projectId) == null) {
            throw new IllegalArgumentException("project not found");
        }
    }

    private void validateTask(Long projectId, Long taskId) {
        if (taskId != null && taskMapper.selectEntityById(projectId, taskId) == null) {
            throw new IllegalArgumentException("task not found: " + taskId);
        }
    }

    private CostPlanVO requirePlan(Long projectId, Long id) {
        CostPlanVO vo = costMapper.selectPlanById(projectId, id);
        if (vo == null) {
            throw new IllegalArgumentException("cost plan not found");
        }
        return vo;
    }

    private CostPlanEntity requirePlanEntity(Long projectId, Long id) {
        CostPlanEntity entity = costMapper.selectPlanEntityById(projectId, id);
        if (entity == null) {
            throw new IllegalArgumentException("cost plan not found");
        }
        return entity;
    }

    private CostActualEntity requireActualEntity(Long projectId, Long id) {
        CostActualEntity entity = costMapper.selectActualEntityById(projectId, id);
        if (entity == null) {
            throw new IllegalArgumentException("cost actual not found");
        }
        return entity;
    }

    private String buildSnapshot(Long projectId, String snapshotJson) {
        if (snapshotJson != null && !snapshotJson.isBlank()) {
            return snapshotJson;
        }
        try {
            return objectMapper.writeValueAsString(costMapper.selectPlansByProjectId(projectId));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to build cost baseline snapshot", e);
        }
    }
}
