package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.dto.SaveProjectEditorPreferenceDto;
import com.manage.managesystem.entity.ProjectEditorPreferenceEntity;
import com.manage.managesystem.mapper.ProjectEditorPreferenceMapper;
import com.manage.managesystem.vo.ProjectEditorPreferenceVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ProjectEditorPreferenceService {
    private static final TypeReference<LinkedHashMap<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ProjectEditorPreferenceMapper projectEditorPreferenceMapper;
    private final ProjectPermissionService projectPermissionService;
    private final ObjectMapper objectMapper;

    public ProjectEditorPreferenceService(ProjectEditorPreferenceMapper projectEditorPreferenceMapper,
                                          ProjectPermissionService projectPermissionService,
                                          ObjectMapper objectMapper) {
        this.projectEditorPreferenceMapper = projectEditorPreferenceMapper;
        this.projectPermissionService = projectPermissionService;
        this.objectMapper = objectMapper;
    }

    public ProjectEditorPreferenceVO get(Long projectId) {
        projectPermissionService.ensureProjectParticipant(projectId);
        projectPermissionService.requireProject(projectId);
        return toVO(projectId, projectEditorPreferenceMapper.selectByProjectId(projectId));
    }

    @Transactional
    public ProjectEditorPreferenceVO save(Long projectId, SaveProjectEditorPreferenceDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        projectPermissionService.requireProject(projectId);
        LocalDateTime now = LocalDateTime.now();
        Long currentUserId = UserContextHolder.getUserId();
        ProjectEditorPreferenceEntity entity = projectEditorPreferenceMapper.selectByProjectId(projectId);
        if (entity == null) {
            entity = new ProjectEditorPreferenceEntity();
            entity.setId(IdWorker.getId());
            entity.setProjectId(projectId);
            entity.setCreatedBy(currentUserId);
            entity.setCreatedAt(now);
            entity.setDeleted(0);
            fillEntity(entity, dto, now, currentUserId);
            projectEditorPreferenceMapper.insert(entity);
        } else {
            fillEntity(entity, dto, now, currentUserId);
            projectEditorPreferenceMapper.update(entity);
        }
        return toVO(projectId, entity);
    }

    private void fillEntity(ProjectEditorPreferenceEntity entity,
                            SaveProjectEditorPreferenceDto dto,
                            LocalDateTime now,
                            Long currentUserId) {
        entity.setGanttAppearanceJson(writeJson(dto == null ? null : dto.getGanttAppearance()));
        entity.setWbsConfigJson(writeJson(dto == null ? null : dto.getWbsConfig()));
        entity.setUpdatedBy(currentUserId);
        entity.setUpdatedAt(now);
    }

    private ProjectEditorPreferenceVO toVO(Long projectId, ProjectEditorPreferenceEntity entity) {
        ProjectEditorPreferenceVO vo = new ProjectEditorPreferenceVO();
        vo.setProjectId(projectId);
        if (entity == null) {
            vo.setGanttAppearance(new LinkedHashMap<>());
            vo.setWbsConfig(new LinkedHashMap<>());
            return vo;
        }
        vo.setGanttAppearance(readJson(entity.getGanttAppearanceJson()));
        vo.setWbsConfig(readJson(entity.getWbsConfigJson()));
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private String writeJson(Map<String, Object> value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid editor preference payload");
        }
    }

    private Map<String, Object> readJson(String raw) {
        if (raw == null || raw.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(raw, MAP_TYPE);
        } catch (Exception ex) {
            return new LinkedHashMap<>();
        }
    }
}
