package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.BaselineQueryDto;
import com.manage.managesystem.dto.ConfigItemQueryDto;
import com.manage.managesystem.dto.CreateBaselineDto;
import com.manage.managesystem.dto.CreateConfigItemDto;
import com.manage.managesystem.dto.CreateCostBaselineDto;
import com.manage.managesystem.dto.CreateScopeBaselineDto;
import com.manage.managesystem.dto.RequirementQueryDto;
import com.manage.managesystem.dto.TaskQueryDto;
import com.manage.managesystem.dto.UpdateConfigItemDto;
import com.manage.managesystem.entity.BaselineRecordEntity;
import com.manage.managesystem.entity.ConfigItemEntity;
import com.manage.managesystem.enums.BaselineStatusEnum;
import com.manage.managesystem.enums.BaselineTypeEnum;
import com.manage.managesystem.enums.ConfigItemStatusEnum;
import com.manage.managesystem.enums.ConfigItemTypeEnum;
import com.manage.managesystem.mapper.ConfigurationMapper;
import com.manage.managesystem.mapper.MilestoneMapper;
import com.manage.managesystem.mapper.ProjectMapper;
import com.manage.managesystem.mapper.RequirementMapper;
import com.manage.managesystem.mapper.ScopeBaselineMapper;
import com.manage.managesystem.mapper.TaskMapper;
import com.manage.managesystem.mapper.WbsMapper;
import com.manage.managesystem.vo.BaselineRecordVO;
import com.manage.managesystem.vo.ConfigItemVO;
import com.manage.managesystem.vo.ScopeBaselineVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigurationService {
    private final ConfigurationMapper configurationMapper;
    private final ProjectMapper projectMapper;
    private final ProjectPermissionService projectPermissionService;
    private final ScopeBaselineService scopeBaselineService;
    private final ScopeBaselineMapper scopeBaselineMapper;
    private final CostService costService;
    private final TaskMapper taskMapper;
    private final MilestoneMapper milestoneMapper;
    private final WbsMapper wbsMapper;
    private final RequirementMapper requirementMapper;
    private final ObjectMapper objectMapper;

    public ConfigurationService(ConfigurationMapper configurationMapper,
                                ProjectMapper projectMapper,
                                ProjectPermissionService projectPermissionService,
                                ScopeBaselineService scopeBaselineService,
                                ScopeBaselineMapper scopeBaselineMapper,
                                CostService costService,
                                TaskMapper taskMapper,
                                MilestoneMapper milestoneMapper,
                                WbsMapper wbsMapper,
                                RequirementMapper requirementMapper,
                                ObjectMapper objectMapper) {
        this.configurationMapper = configurationMapper;
        this.projectMapper = projectMapper;
        this.projectPermissionService = projectPermissionService;
        this.scopeBaselineService = scopeBaselineService;
        this.scopeBaselineMapper = scopeBaselineMapper;
        this.costService = costService;
        this.taskMapper = taskMapper;
        this.milestoneMapper = milestoneMapper;
        this.wbsMapper = wbsMapper;
        this.requirementMapper = requirementMapper;
        this.objectMapper = objectMapper;
    }

    public PageResult<ConfigItemVO> listConfigItems(Long projectId, ConfigItemQueryDto queryDto) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProjectExists(projectId);
        return buildPageResult(
                configurationMapper.selectConfigItemsByProjectId(projectId, queryDto),
                queryDto.getPage(),
                queryDto.getPageSize()
        );
    }

    @Transactional
    public ConfigItemVO createConfigItem(Long projectId, CreateConfigItemDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);
        LocalDateTime now = LocalDateTime.now();
        Long userId = UserContextHolder.getUserId();
        ConfigItemEntity entity = new ConfigItemEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setItemName(dto.getItemName().trim());
        entity.setItemType(normalizeConfigItemType(dto.getItemType()));
        entity.setVersionNo(normalizeText(dto.getVersionNo()));
        entity.setStatus(normalizeConfigItemStatus(dto.getStatus()));
        entity.setRepositoryUrl(normalizeText(dto.getRepositoryUrl()));
        entity.setRemark(normalizeText(dto.getRemark()));
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        configurationMapper.insertConfigItem(entity);
        return requireConfigItem(projectId, entity.getId());
    }

    @Transactional
    public ConfigItemVO updateConfigItem(Long projectId, Long id, UpdateConfigItemDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);
        ConfigItemEntity entity = requireConfigItemEntity(projectId, id);
        entity.setItemName(dto.getItemName().trim());
        entity.setItemType(normalizeConfigItemType(dto.getItemType()));
        entity.setVersionNo(normalizeText(dto.getVersionNo()));
        entity.setStatus(normalizeConfigItemStatus(dto.getStatus()));
        entity.setRepositoryUrl(normalizeText(dto.getRepositoryUrl()));
        entity.setRemark(normalizeText(dto.getRemark()));
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(LocalDateTime.now());
        configurationMapper.updateConfigItem(entity);
        return requireConfigItem(projectId, id);
    }

    @Transactional
    public void deleteConfigItem(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);
        requireConfigItemEntity(projectId, id);
        configurationMapper.softDeleteConfigItem(projectId, id, UserContextHolder.getUserId(), LocalDateTime.now());
    }

    public PageResult<BaselineRecordVO> listBaselines(Long projectId, BaselineQueryDto queryDto) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProjectExists(projectId);
        List<BaselineRecordVO> list = new ArrayList<>(configurationMapper.selectBaselineRecordsByProjectId(projectId, queryDto));

        String baselineType = normalizeOptionalUpper(queryDto.getBaselineType());
        if (baselineType == null || BaselineTypeEnum.SCOPE.name().equals(baselineType)) {
            for (ScopeBaselineVO scopeBaseline : scopeBaselineMapper.selectByProjectId(projectId)) {
                if (queryDto.getStatus() != null && !queryDto.getStatus().isBlank()
                        && !queryDto.getStatus().trim().equalsIgnoreCase(scopeBaseline.getStatus())) {
                    continue;
                }
                list.add(mapScopeBaseline(scopeBaseline));
            }
        }

        list.sort(
                Comparator.comparing(BaselineRecordVO::getPublishedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(BaselineRecordVO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(BaselineRecordVO::getId, Comparator.nullsLast(Comparator.reverseOrder()))
        );

        return buildPageResult(list, queryDto.getPage(), queryDto.getPageSize());
    }

    @Transactional
    public BaselineRecordVO createBaseline(Long projectId, CreateBaselineDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);
        String baselineType = normalizeBaselineType(dto.getBaselineType());
        if (BaselineTypeEnum.SCOPE.name().equals(baselineType)) {
            CreateScopeBaselineDto scopeDto = new CreateScopeBaselineDto();
            scopeDto.setBaselineName(dto.getBaselineName().trim());
            scopeDto.setDescription(normalizeText(dto.getDescription()));
            scopeDto.setSnapshotJson(buildScopeSnapshot(projectId, dto.getSnapshotJson()));
            return mapScopeBaseline(scopeBaselineService.create(projectId, scopeDto));
        }
        if (BaselineTypeEnum.COST.name().equals(baselineType)) {
            CreateCostBaselineDto costDto = new CreateCostBaselineDto();
            costDto.setBaselineName(dto.getBaselineName().trim());
            costDto.setDescription(normalizeText(dto.getDescription()));
            costDto.setSnapshotJson(normalizeText(dto.getSnapshotJson()));
            costService.createBaseline(projectId, costDto);
            return requireBaselineRecord(projectId, findLatestBaselineId(projectId, BaselineTypeEnum.COST.name()));
        }

        LocalDateTime now = LocalDateTime.now();
        BaselineRecordEntity entity = new BaselineRecordEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setBaselineType(baselineType);
        entity.setVersionNo("V" + (configurationMapper.countBaselineRecords(projectId, baselineType) + 1));
        entity.setBaselineName(dto.getBaselineName().trim());
        entity.setSnapshotJson(buildBaselineSnapshot(projectId, baselineType, dto.getSnapshotJson()));
        entity.setStatus(BaselineStatusEnum.PUBLISHED.name());
        entity.setPublishedBy(UserContextHolder.getUserId());
        entity.setPublishedAt(now);
        entity.setCreatedAt(now);
        configurationMapper.insertBaselineRecord(entity);
        return requireBaselineRecord(projectId, entity.getId());
    }

    @Transactional
    public void deleteBaseline(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);

        if (scopeBaselineMapper.selectEntityById(id) != null && projectId.equals(scopeBaselineMapper.selectEntityById(id).getProjectId())) {
            scopeBaselineService.delete(projectId, id);
            return;
        }

        BaselineRecordEntity entity = configurationMapper.selectBaselineRecordEntityById(projectId, id);
        if (entity == null) {
            throw new IllegalArgumentException("baseline not found");
        }
        if (BaselineTypeEnum.COST.name().equals(entity.getBaselineType())) {
            costService.deleteBaseline(projectId, id);
            return;
        }
        configurationMapper.deleteBaselineRecord(projectId, id);
    }

    @Transactional
    public void restoreBaseline(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);

        BaselineRecordEntity entity = configurationMapper.selectBaselineRecordEntityById(projectId, id);
        if (entity == null) {
            throw new IllegalArgumentException("baseline not found");
        }
        if (!BaselineTypeEnum.CONFIG.name().equals(entity.getBaselineType())) {
            throw new IllegalArgumentException("only config baseline can be restored");
        }

        List<ConfigItemVO> snapshotItems = parseConfigBaselineSnapshot(entity.getSnapshotJson());
        LocalDateTime now = LocalDateTime.now();
        Long userId = UserContextHolder.getUserId();

        configurationMapper.softDeleteConfigItemsByProjectId(projectId, userId, now);
        for (ConfigItemVO snapshotItem : snapshotItems) {
            configurationMapper.insertConfigItem(buildRestoredConfigItem(projectId, snapshotItem, userId, now));
        }
    }

    private void ensureProjectExists(Long projectId) {
        if (projectMapper.selectEntityById(projectId) == null) {
            throw new IllegalArgumentException("project not found");
        }
    }

    private ConfigItemVO requireConfigItem(Long projectId, Long id) {
        ConfigItemVO vo = configurationMapper.selectConfigItemById(projectId, id);
        if (vo == null) {
            throw new IllegalArgumentException("config item not found");
        }
        return vo;
    }

    private ConfigItemEntity requireConfigItemEntity(Long projectId, Long id) {
        ConfigItemEntity entity = configurationMapper.selectConfigItemEntityById(projectId, id);
        if (entity == null) {
            throw new IllegalArgumentException("config item not found");
        }
        return entity;
    }

    private BaselineRecordVO requireBaselineRecord(Long projectId, Long id) {
        BaselineRecordVO vo = configurationMapper.selectBaselineRecordById(projectId, id);
        if (vo == null) {
            throw new IllegalArgumentException("baseline not found");
        }
        return vo;
    }

    private Long findLatestBaselineId(Long projectId, String baselineType) {
        return configurationMapper.selectBaselineRecordsByProjectId(projectId, newBaselineQuery(baselineType)).stream()
                .findFirst()
                .map(BaselineRecordVO::getId)
                .orElseThrow(() -> new IllegalStateException("baseline create failed"));
    }

    private BaselineQueryDto newBaselineQuery(String baselineType) {
        BaselineQueryDto queryDto = new BaselineQueryDto();
        queryDto.setBaselineType(baselineType);
        return queryDto;
    }

    private String buildBaselineSnapshot(Long projectId, String baselineType, String snapshotJson) {
        if (snapshotJson != null && !snapshotJson.isBlank()) {
            return snapshotJson;
        }
        try {
            if (BaselineTypeEnum.CONFIG.name().equals(baselineType)) {
                ConfigItemQueryDto queryDto = new ConfigItemQueryDto();
                queryDto.setPageSize(500);
                return objectMapper.writeValueAsString(configurationMapper.selectConfigItemsByProjectId(projectId, queryDto));
            }
            if (BaselineTypeEnum.SCHEDULE.name().equals(baselineType)) {
                Map<String, Object> snapshot = new LinkedHashMap<>();
                TaskQueryDto taskQueryDto = new TaskQueryDto();
                taskQueryDto.setPageSize(1000);
                snapshot.put("tasks", taskMapper.selectByProjectId(projectId, taskQueryDto));
                snapshot.put("milestones", milestoneMapper.selectByProjectId(projectId));
                snapshot.put("dependencies", taskMapper.selectDependenciesByProjectId(projectId));
                return objectMapper.writeValueAsString(snapshot);
            }
            return "{}";
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to build baseline snapshot", e);
        }
    }

    private List<ConfigItemVO> parseConfigBaselineSnapshot(String snapshotJson) {
        if (snapshotJson == null || snapshotJson.isBlank()) {
            return List.of();
        }
        try {
            List<ConfigItemVO> snapshotItems = objectMapper.readValue(snapshotJson, new TypeReference<List<ConfigItemVO>>() {
            });
            return snapshotItems == null ? List.of() : snapshotItems;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to parse config baseline snapshot", e);
        }
    }

    private ConfigItemEntity buildRestoredConfigItem(Long projectId,
                                                     ConfigItemVO snapshotItem,
                                                     Long operatorId,
                                                     LocalDateTime now) {
        if (snapshotItem == null || snapshotItem.getItemName() == null || snapshotItem.getItemName().isBlank()) {
            throw new IllegalStateException("config baseline snapshot item is invalid");
        }

        ConfigItemEntity entity = new ConfigItemEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setItemName(snapshotItem.getItemName().trim());
        entity.setItemType(normalizeConfigItemType(snapshotItem.getItemType()));
        entity.setVersionNo(normalizeText(snapshotItem.getVersionNo()));
        entity.setStatus(normalizeConfigItemStatus(snapshotItem.getStatus()));
        entity.setRepositoryUrl(normalizeText(snapshotItem.getRepositoryUrl()));
        entity.setRemark(normalizeText(snapshotItem.getRemark()));
        entity.setCreatedBy(snapshotItem.getCreatedBy() == null ? operatorId : snapshotItem.getCreatedBy());
        entity.setCreatedAt(snapshotItem.getCreatedAt() == null ? now : snapshotItem.getCreatedAt());
        entity.setUpdatedBy(operatorId);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        return entity;
    }

    private String buildScopeSnapshot(Long projectId, String snapshotJson) {
        if (snapshotJson != null && !snapshotJson.isBlank()) {
            return snapshotJson;
        }
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            RequirementQueryDto requirementQueryDto = new RequirementQueryDto();
            requirementQueryDto.setPageSize(1000);
            TaskQueryDto taskQueryDto = new TaskQueryDto();
            taskQueryDto.setPageSize(1000);
            snapshot.put("wbsNodes", wbsMapper.selectByProjectId(projectId));
            snapshot.put("requirements", requirementMapper.selectByProjectId(projectId, requirementQueryDto));
            snapshot.put("tasks", taskMapper.selectByProjectId(projectId, taskQueryDto));
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to build scope baseline snapshot", e);
        }
    }

    private BaselineRecordVO mapScopeBaseline(ScopeBaselineVO scopeBaseline) {
        BaselineRecordVO vo = new BaselineRecordVO();
        vo.setId(scopeBaseline.getId());
        vo.setProjectId(scopeBaseline.getProjectId());
        vo.setBaselineType(BaselineTypeEnum.SCOPE.name());
        vo.setVersionNo(scopeBaseline.getVersionNo());
        vo.setBaselineName(scopeBaseline.getBaselineName());
        vo.setDescription(scopeBaseline.getDescription());
        vo.setSnapshotJson(scopeBaseline.getSnapshotJson());
        vo.setStatus(scopeBaseline.getStatus());
        vo.setPublishedBy(scopeBaseline.getPublishedBy());
        vo.setPublishedAt(scopeBaseline.getPublishedAt());
        vo.setCreatedAt(scopeBaseline.getPublishedAt());
        return vo;
    }

    private String normalizeConfigItemType(String itemType) {
        try {
            return ConfigItemTypeEnum.valueOf(itemType.trim().toUpperCase()).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid config item type");
        }
    }

    private String normalizeConfigItemStatus(String status) {
        if (status == null || status.isBlank()) {
            return ConfigItemStatusEnum.DRAFT.name();
        }
        try {
            return ConfigItemStatusEnum.valueOf(status.trim().toUpperCase()).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid config item status");
        }
    }

    private String normalizeBaselineType(String baselineType) {
        try {
            return BaselineTypeEnum.valueOf(baselineType.trim().toUpperCase()).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid baseline type");
        }
    }

    private String normalizeOptionalUpper(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
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
