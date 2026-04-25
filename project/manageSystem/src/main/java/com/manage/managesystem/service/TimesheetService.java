package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.CreateTimesheetDto;
import com.manage.managesystem.dto.TimesheetQueryDto;
import com.manage.managesystem.dto.UpdateTimesheetDto;
import com.manage.managesystem.entity.TaskEntity;
import com.manage.managesystem.entity.TimesheetEntity;
import com.manage.managesystem.mapper.CostMapper;
import com.manage.managesystem.mapper.TaskMapper;
import com.manage.managesystem.mapper.TimesheetMapper;
import com.manage.managesystem.vo.TimesheetReportVO;
import com.manage.managesystem.vo.TimesheetVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TimesheetService {
    private final TimesheetMapper timesheetMapper;
    private final TaskMapper taskMapper;
    private final CostMapper costMapper;

    public TimesheetService(TimesheetMapper timesheetMapper, TaskMapper taskMapper, CostMapper costMapper) {
        this.timesheetMapper = timesheetMapper;
        this.taskMapper = taskMapper;
        this.costMapper = costMapper;
    }

    public PageResult<TimesheetVO> list(Long projectId, TimesheetQueryDto queryDto) {
        List<TimesheetVO> list = timesheetMapper.selectByProjectId(projectId, queryDto);
        PageResult<TimesheetVO> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setPage(queryDto.getPage() == null ? 1 : queryDto.getPage());
        pageResult.setPageSize(queryDto.getPageSize() == null ? list.size() : queryDto.getPageSize());
        pageResult.setTotal((long) list.size());
        return pageResult;
    }

    @Transactional
    public TimesheetVO create(Long projectId, CreateTimesheetDto dto) {
        TaskEntity task = requireTask(projectId, dto.getTaskId());
        LocalDateTime now = LocalDateTime.now();

        TimesheetEntity entity = new TimesheetEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setTaskId(dto.getTaskId());
        entity.setUserId(UserContextHolder.getUserId());
        entity.setWorkDate(dto.getWorkDate());
        entity.setHours(dto.getHours());
        entity.setCostRate(resolveCostRate(projectId, task));
        entity.setLaborCost(resolveLaborCost(dto.getHours(), entity.getCostRate()));
        entity.setDescription(dto.getDescription());
        entity.setStatus("SUBMITTED");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        timesheetMapper.insert(entity);

        syncTaskActualMetrics(projectId, dto.getTaskId());
        return timesheetMapper.selectById(entity.getId());
    }

    @Transactional
    public TimesheetVO update(Long projectId, Long id, UpdateTimesheetDto dto) {
        TaskEntity targetTask = requireTask(projectId, dto.getTaskId());
        TimesheetEntity entity = ensureRecord(projectId, id);
        Long previousTaskId = entity.getTaskId();

        entity.setTaskId(dto.getTaskId());
        entity.setWorkDate(dto.getWorkDate());
        entity.setHours(dto.getHours());
        entity.setCostRate(resolveCostRate(projectId, targetTask));
        entity.setLaborCost(resolveLaborCost(dto.getHours(), entity.getCostRate()));
        entity.setDescription(dto.getDescription());
        entity.setUpdatedAt(LocalDateTime.now());
        timesheetMapper.update(entity);

        if (previousTaskId != null && !previousTaskId.equals(dto.getTaskId())) {
            syncTaskActualMetrics(projectId, previousTaskId);
        }
        syncTaskActualMetrics(projectId, dto.getTaskId());
        return timesheetMapper.selectById(id);
    }

    @Transactional
    public void delete(Long projectId, Long id) {
        TimesheetEntity entity = ensureRecord(projectId, id);
        timesheetMapper.softDelete(id, LocalDateTime.now());
        if (entity.getTaskId() != null) {
            syncTaskActualMetrics(projectId, entity.getTaskId());
        }
    }

    public TimesheetReportVO report(Long projectId) {
        TimesheetQueryDto queryDto = new TimesheetQueryDto();
        List<TimesheetVO> records = timesheetMapper.selectByProjectId(projectId, queryDto);
        TimesheetReportVO report = new TimesheetReportVO();
        BigDecimal totalHours = timesheetMapper.sumHoursByProjectId(projectId);
        report.setTotalHours(totalHours == null ? BigDecimal.ZERO : totalHours);
        BigDecimal totalLaborCost = timesheetMapper.sumLaborCostByProjectId(projectId);
        report.setTotalLaborCost(totalLaborCost == null ? BigDecimal.ZERO : totalLaborCost);
        Integer userCount = timesheetMapper.countDistinctUsers(projectId);
        report.setUserCount(userCount == null ? 0 : userCount);
        report.setRecords(records);
        return report;
    }

    private TimesheetEntity ensureRecord(Long projectId, Long id) {
        TimesheetEntity entity = timesheetMapper.selectEntityById(id);
        if (entity == null || !projectId.equals(entity.getProjectId())) {
            throw new IllegalArgumentException("timesheet record not found");
        }
        return entity;
    }

    private TaskEntity requireTask(Long projectId, Long taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("task not found");
        }
        TaskEntity task = taskMapper.selectEntityById(projectId, taskId);
        if (task == null) {
            throw new IllegalArgumentException("task not found");
        }
        return task;
    }

    private BigDecimal resolveCostRate(Long projectId, TaskEntity task) {
        BigDecimal plannedHours = task.getPlannedHours();
        if (plannedHours == null || plannedHours.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal laborPlanAmount = costMapper.sumPlannedLaborAmountByTask(projectId, task.getId());
        if (laborPlanAmount != null && laborPlanAmount.compareTo(BigDecimal.ZERO) > 0) {
            return laborPlanAmount.divide(plannedHours, 2, RoundingMode.HALF_UP);
        }

        BigDecimal plannedCost = task.getPlannedCost();
        if (plannedCost != null && plannedCost.compareTo(BigDecimal.ZERO) > 0) {
            return plannedCost.divide(plannedHours, 2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal resolveLaborCost(BigDecimal hours, BigDecimal costRate) {
        BigDecimal safeHours = hours == null ? BigDecimal.ZERO : hours;
        BigDecimal safeRate = costRate == null ? BigDecimal.ZERO : costRate;
        return safeHours.multiply(safeRate).setScale(2, RoundingMode.HALF_UP);
    }

    private void syncTaskActualMetrics(Long projectId, Long taskId) {
        TaskEntity task = taskMapper.selectEntityById(projectId, taskId);
        if (task == null) {
            return;
        }
        BigDecimal actualHours = timesheetMapper.sumHoursByTaskId(projectId, taskId);
        BigDecimal actualCost = timesheetMapper.sumLaborCostByTaskId(projectId, taskId);
        taskMapper.updateActualMetrics(
                projectId,
                taskId,
                actualHours == null ? BigDecimal.ZERO : actualHours,
                actualCost == null ? BigDecimal.ZERO : actualCost,
                UserContextHolder.getUserId(),
                LocalDateTime.now()
        );
    }
}
