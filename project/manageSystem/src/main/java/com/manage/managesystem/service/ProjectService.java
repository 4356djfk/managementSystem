package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.ChangeProjectStatusDto;
import com.manage.managesystem.dto.CreateProjectDto;
import com.manage.managesystem.dto.CreateProjectFromTemplateDto;
import com.manage.managesystem.dto.ProjectQueryDto;
import com.manage.managesystem.dto.SaveProjectCharterDto;
import com.manage.managesystem.dto.UpdateProjectDto;
import com.manage.managesystem.entity.ProjectCharterEntity;
import com.manage.managesystem.entity.ProjectEntity;
import com.manage.managesystem.enums.ProjectStatusEnum;
import com.manage.managesystem.enums.ProjectTemplateStatusEnum;
import com.manage.managesystem.mapper.ProjectMapper;
import com.manage.managesystem.mapper.ProjectTemplateMapper;
import com.manage.managesystem.mapper.UserMapper;
import com.manage.managesystem.vo.ProjectCharterVO;
import com.manage.managesystem.vo.ProjectDashboardVO;
import com.manage.managesystem.vo.ProjectDetailVO;
import com.manage.managesystem.vo.ProjectListItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ProjectService {
    private final ProjectMapper projectMapper;
    private final ProjectTemplateMapper projectTemplateMapper;
    private final UserMapper userMapper;
    private final ProjectPermissionService projectPermissionService;

    public ProjectService(ProjectMapper projectMapper,
                          ProjectTemplateMapper projectTemplateMapper,
                          UserMapper userMapper,
                          ProjectPermissionService projectPermissionService) {
        this.projectMapper = projectMapper;
        this.projectTemplateMapper = projectTemplateMapper;
        this.userMapper = userMapper;
        this.projectPermissionService = projectPermissionService;
    }

    public PageResult<ProjectListItemVO> list(ProjectQueryDto queryDto) {
        List<ProjectListItemVO> list = projectMapper.selectList(queryDto);
        Long currentUserId = projectPermissionService.requireCurrentUserId();
        if (!projectPermissionService.hasCurrentUserBusinessRole()) {
            list = List.of();
        } else {
            list = list.stream()
                    .filter(item -> Objects.equals(item.getOwnerId(), currentUserId)
                            || projectPermissionService.isActiveParticipant(item.getId(), currentUserId))
                    .toList();
        }
        PageResult<ProjectListItemVO> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setPage(queryDto.getPage() == null ? 1 : queryDto.getPage());
        pageResult.setPageSize(queryDto.getPageSize() == null ? list.size() : queryDto.getPageSize());
        pageResult.setTotal((long) list.size());
        return pageResult;
    }

    public ProjectDetailVO detail(Long projectId) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ProjectDetailVO detail = projectMapper.selectDetailById(projectId);
        if (detail == null) {
            throw new IllegalArgumentException("项目不存在");
        }
        return detail;
    }

    @Transactional
    public ProjectDetailVO create(CreateProjectDto dto) {
        projectPermissionService.ensureCurrentUserHasBusinessRole();
        Long ownerId = projectPermissionService.requireCurrentUserId();
        validateTemplate(dto.getTemplateId());
        return createProject(
                dto.getName(),
                dto.getDescription(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getLifeCycleModel(),
                ownerId,
                dto.getTemplateId(),
                dto.getPlannedBudget(),
                0
        );
    }

    @Transactional
    public ProjectDetailVO update(Long projectId, UpdateProjectDto dto) {
        projectPermissionService.ensureProjectOwner(projectId);
        ProjectEntity entity = ensureProjectExists(projectId);
        validateOwner(dto.getOwnerId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setLifeCycleModel(dto.getLifeCycleModel());
        entity.setOwnerId(dto.getOwnerId() == null ? entity.getOwnerId() : dto.getOwnerId());
        entity.setPlannedBudget(dto.getPlannedBudget() == null ? BigDecimal.ZERO : dto.getPlannedBudget());
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(LocalDateTime.now());
        projectMapper.update(entity);
        return detail(projectId);
    }

    @Transactional
    public void changeStatus(Long projectId, ChangeProjectStatusDto dto) {
        projectPermissionService.ensureProjectOwner(projectId);
        ProjectEntity entity = ensureProjectExists(projectId);
        ProjectStatusEnum currentStatus = parseProjectStatus(entity.getStatus());
        ProjectStatusEnum targetStatus = parseProjectStatus(dto.getStatus());
        validateStatusTransition(currentStatus, targetStatus);

        LocalDate actualStartDate = null;
        LocalDate actualEndDate = null;
        boolean clearActualEndDate = false;
        if (targetStatus == ProjectStatusEnum.IN_PROGRESS && entity.getActualStartDate() == null) {
            actualStartDate = LocalDate.now();
        }
        if (targetStatus == ProjectStatusEnum.CLOSED) {
            actualEndDate = LocalDate.now();
        } else if (currentStatus == ProjectStatusEnum.CLOSED) {
            clearActualEndDate = true;
        }

        projectMapper.updateStatus(
                projectId,
                targetStatus.name(),
                actualStartDate,
                actualEndDate,
                clearActualEndDate,
                UserContextHolder.getUserId(),
                LocalDateTime.now()
        );
    }

    @Transactional
    public void close(Long projectId) {
        ChangeProjectStatusDto dto = new ChangeProjectStatusDto();
        dto.setStatus(ProjectStatusEnum.CLOSED.name());
        changeStatus(projectId, dto);
    }

    @Transactional
    public void delete(Long projectId) {
        projectPermissionService.ensureProjectOwner(projectId);
        ensureProjectExists(projectId);
        projectMapper.softDelete(projectId, UserContextHolder.getUserId(), LocalDateTime.now());
    }

    public ProjectCharterVO getCharter(Long projectId) {
        projectPermissionService.ensureProjectParticipant(projectId);
        return projectMapper.selectCharterByProjectId(projectId);
    }

    @Transactional
    public ProjectCharterVO saveCharter(Long projectId, SaveProjectCharterDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);
        ProjectCharterVO existing = projectMapper.selectCharterByProjectId(projectId);
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            ProjectCharterEntity entity = new ProjectCharterEntity();
            entity.setId(IdWorker.getId());
            entity.setProjectId(projectId);
            fillCharter(entity, dto, now, true);
            projectMapper.insertCharter(entity);
        } else {
            ProjectCharterEntity entity = new ProjectCharterEntity();
            entity.setId(existing.getId());
            entity.setProjectId(projectId);
            fillCharter(entity, dto, now, false);
            projectMapper.updateCharter(entity);
        }
        return projectMapper.selectCharterByProjectId(projectId);
    }

    public ProjectDashboardVO dashboard(Long projectId) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ProjectDashboardVO dashboard = projectMapper.selectDashboard(projectId);
        if (dashboard == null) {
            dashboard = new ProjectDashboardVO();
        }
        dashboard.setUpcomingMilestones(projectMapper.selectUpcomingMilestones(projectId));
        return dashboard;
    }

    @Transactional
    public ProjectDetailVO createFromTemplate(CreateProjectFromTemplateDto dto) {
        projectPermissionService.ensureCurrentUserHasBusinessRole();
        Long ownerId = projectPermissionService.requireCurrentUserId();
        var template = validateTemplate(dto.getTemplateId());
        return createProject(
                dto.getName(),
                dto.getDescription(),
                dto.getStartDate(),
                dto.getEndDate(),
                template.getType(),
                ownerId,
                dto.getTemplateId(),
                dto.getPlannedBudget(),
                0
        );
    }

    @Transactional
    public ProjectDetailVO initDemo() {
        projectPermissionService.ensureCurrentUserHasBusinessRole();
        Long ownerId = projectPermissionService.requireCurrentUserId();
        var template = projectTemplateMapper.selectFirstEnabled();
        Long templateId = template == null ? null : template.getId();
        String lifeCycleModel = template == null ? "AGILE" : template.getType();
        return createProject(
                "演示项目-" + System.currentTimeMillis(),
                "系统自动初始化的演示项目",
                null,
                null,
                lifeCycleModel,
                ownerId,
                templateId,
                BigDecimal.ZERO,
                1
        );
    }

    private ProjectDetailVO createProject(
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            String lifeCycleModel,
            Long ownerId,
            Long templateId,
            BigDecimal plannedBudget,
            Integer isDemo
    ) {
        LocalDateTime now = LocalDateTime.now();
        ProjectEntity entity = new ProjectEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectCode("PRJ" + entity.getId());
        entity.setName(name);
        entity.setDescription(description);
        entity.setLifeCycleModel(lifeCycleModel);
        entity.setStatus(ProjectStatusEnum.PLANNING.name());
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setOwnerId(ownerId);
        entity.setTemplateId(templateId);
        entity.setProgressRate(BigDecimal.ZERO);
        entity.setPlannedBudget(plannedBudget == null ? BigDecimal.ZERO : plannedBudget);
        entity.setActualCost(BigDecimal.ZERO);
        entity.setIsDemo(isDemo);
        entity.setCreatedBy(UserContextHolder.getUserId());
        entity.setCreatedAt(now);
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        projectMapper.insert(entity);
        return detail(entity.getId());
    }

    private void fillCharter(ProjectCharterEntity entity, SaveProjectCharterDto dto, LocalDateTime now, boolean create) {
        entity.setObjective(dto.getObjective());
        entity.setScopeSummary(dto.getScopeSummary());
        entity.setSponsor(dto.getSponsor());
        entity.setApprover(dto.getApprover());
        entity.setStakeholders(dto.getStakeholders());
        entity.setSuccessCriteria(dto.getSuccessCriteria());
        if (create) {
            entity.setCreatedBy(UserContextHolder.getUserId());
            entity.setCreatedAt(now);
        }
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(now);
    }

    private ProjectEntity ensureProjectExists(Long projectId) {
        ProjectEntity entity = projectMapper.selectEntityById(projectId);
        if (entity == null) {
            throw new IllegalArgumentException("项目不存在");
        }
        return entity;
    }

    private com.manage.managesystem.entity.ProjectTemplateEntity validateTemplate(Long templateId) {
        if (templateId == null) {
            return null;
        }
        var template = projectTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new IllegalArgumentException("项目模板不存在");
        }
        if (!ProjectTemplateStatusEnum.ENABLED.name().equals(template.getStatus())) {
            throw new IllegalArgumentException("项目模板未启用");
        }
        return template;
    }

    private void validateOwner(Long ownerId) {
        if (ownerId == null) {
            return;
        }
        if (userMapper.selectById(ownerId) == null) {
            throw new IllegalArgumentException("项目负责人不存在: " + ownerId);
        }
        projectPermissionService.ensureBusinessUser(ownerId, "project owner must have USER role");
    }

    private ProjectStatusEnum parseProjectStatus(String status) {
        try {
            return ProjectStatusEnum.valueOf(status);
        } catch (Exception ex) {
            throw new IllegalArgumentException("非法的项目状态");
        }
    }

    private void validateStatusTransition(ProjectStatusEnum currentStatus, ProjectStatusEnum targetStatus) {
        if (currentStatus == targetStatus) {
            return;
        }
        boolean valid = switch (currentStatus) {
            case PLANNING -> targetStatus == ProjectStatusEnum.IN_PROGRESS;
            case IN_PROGRESS -> targetStatus == ProjectStatusEnum.MONITORING || targetStatus == ProjectStatusEnum.CLOSED;
            case MONITORING -> targetStatus == ProjectStatusEnum.CLOSED;
            case CLOSED -> targetStatus != ProjectStatusEnum.CLOSED;
        };
        if (!valid) {
            throw new IllegalArgumentException("不允许当前项目状态执行该流转");
        }
    }
}
