package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.dto.CreateScopeBaselineDto;
import com.manage.managesystem.entity.ScopeBaselineEntity;
import com.manage.managesystem.mapper.ProjectMapper;
import com.manage.managesystem.mapper.ScopeBaselineMapper;
import com.manage.managesystem.vo.ScopeBaselineVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScopeBaselineService {
    private static final String VERSION_PREFIX = "V";
    private static final String VERSION_UNIQUE_KEY = "uk_scope_baseline_version";
    private static final int MAX_VERSION_RETRIES = 3;

    private final ScopeBaselineMapper scopeBaselineMapper;
    private final ProjectMapper projectMapper;
    private final ProjectPermissionService projectPermissionService;

    public ScopeBaselineService(ScopeBaselineMapper scopeBaselineMapper,
                                ProjectMapper projectMapper,
                                ProjectPermissionService projectPermissionService) {
        this.scopeBaselineMapper = scopeBaselineMapper;
        this.projectMapper = projectMapper;
        this.projectPermissionService = projectPermissionService;
    }

    public List<ScopeBaselineVO> list(Long projectId) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProjectExists(projectId);
        return scopeBaselineMapper.selectByProjectId(projectId);
    }

    @Transactional
    public ScopeBaselineVO create(Long projectId, CreateScopeBaselineDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);
        LocalDateTime now = LocalDateTime.now();

        ScopeBaselineEntity entity = new ScopeBaselineEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setBaselineName(dto.getBaselineName());
        entity.setDescription(dto.getDescription());
        entity.setSnapshotJson(dto.getSnapshotJson() == null || dto.getSnapshotJson().isBlank() ? "{}" : dto.getSnapshotJson());
        entity.setStatus("PUBLISHED");
        entity.setPublishedBy(UserContextHolder.getUserId());
        entity.setPublishedAt(now);
        entity.setCreatedAt(now);
        insertWithNextVersion(entity);
        return scopeBaselineMapper.selectByProjectId(projectId).stream()
                .filter(item -> item.getId().equals(entity.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("scope baseline create failed"));
    }

    @Transactional
    public void delete(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProjectExists(projectId);
        ScopeBaselineEntity entity = scopeBaselineMapper.selectEntityById(id);
        if (entity == null || !projectId.equals(entity.getProjectId())) {
            throw new IllegalArgumentException("scope baseline not found");
        }
        scopeBaselineMapper.deleteById(id);
    }

    private void ensureProjectExists(Long projectId) {
        if (projectMapper.selectEntityById(projectId) == null) {
            throw new IllegalArgumentException("project not found");
        }
    }

    private void insertWithNextVersion(ScopeBaselineEntity entity) {
        for (int attempt = 0; attempt < MAX_VERSION_RETRIES; attempt++) {
            entity.setVersionNo(nextVersionNo(entity.getProjectId()));
            try {
                scopeBaselineMapper.insert(entity);
                return;
            } catch (DuplicateKeyException ex) {
                if (!isVersionConflict(ex) || attempt == MAX_VERSION_RETRIES - 1) {
                    throw ex;
                }
            }
        }
    }

    private String nextVersionNo(Long projectId) {
        Integer maxVersionNumber = scopeBaselineMapper.selectMaxVersionNumber(projectId);
        int nextVersionNumber = (maxVersionNumber == null ? 0 : maxVersionNumber) + 1;
        return VERSION_PREFIX + nextVersionNumber;
    }

    private boolean isVersionConflict(DuplicateKeyException ex) {
        Throwable mostSpecificCause = ex.getMostSpecificCause();
        String message = mostSpecificCause == null ? ex.getMessage() : mostSpecificCause.getMessage();
        return message != null && message.contains(VERSION_UNIQUE_KEY);
    }
}
