package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.dto.CreateScopeBaselineDto;
import com.manage.managesystem.entity.ScopeBaselineEntity;
import com.manage.managesystem.mapper.ProjectMapper;
import com.manage.managesystem.mapper.ScopeBaselineMapper;
import com.manage.managesystem.vo.ScopeBaselineVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScopeBaselineService {
    private final ScopeBaselineMapper scopeBaselineMapper;
    private final ProjectMapper projectMapper;

    public ScopeBaselineService(ScopeBaselineMapper scopeBaselineMapper, ProjectMapper projectMapper) {
        this.scopeBaselineMapper = scopeBaselineMapper;
        this.projectMapper = projectMapper;
    }

    public List<ScopeBaselineVO> list(Long projectId) {
        ensureProjectExists(projectId);
        return scopeBaselineMapper.selectByProjectId(projectId);
    }

    @Transactional
    public ScopeBaselineVO create(Long projectId, CreateScopeBaselineDto dto) {
        ensureProjectExists(projectId);
        LocalDateTime now = LocalDateTime.now();

        ScopeBaselineEntity entity = new ScopeBaselineEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setVersionNo("V" + (scopeBaselineMapper.countByProjectId(projectId) + 1));
        entity.setBaselineName(dto.getBaselineName());
        entity.setDescription(dto.getDescription());
        entity.setSnapshotJson(dto.getSnapshotJson() == null || dto.getSnapshotJson().isBlank() ? "{}" : dto.getSnapshotJson());
        entity.setStatus("PUBLISHED");
        entity.setPublishedBy(UserContextHolder.getUserId());
        entity.setPublishedAt(now);
        entity.setCreatedAt(now);
        scopeBaselineMapper.insert(entity);
        return scopeBaselineMapper.selectByProjectId(projectId).stream()
                .filter(item -> item.getId().equals(entity.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("scope baseline create failed"));
    }

    @Transactional
    public void delete(Long projectId, Long id) {
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
}
