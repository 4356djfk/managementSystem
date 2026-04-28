package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.dto.CreateWbsNodeDto;
import com.manage.managesystem.dto.UpdateWbsNodeDto;
import com.manage.managesystem.entity.WbsNodeEntity;
import com.manage.managesystem.mapper.WbsMapper;
import com.manage.managesystem.vo.WbsNodeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WbsService {
    private final WbsMapper wbsMapper;
    private final ProjectPermissionService projectPermissionService;

    public WbsService(WbsMapper wbsMapper, ProjectPermissionService projectPermissionService) {
        this.wbsMapper = wbsMapper;
        this.projectPermissionService = projectPermissionService;
    }

    public List<WbsNodeVO> list(Long projectId) {
        projectPermissionService.ensureProjectParticipant(projectId);
        List<WbsNodeVO> flatList = wbsMapper.selectByProjectId(projectId);
        Map<Long, WbsNodeVO> nodeMap = new HashMap<>();
        List<WbsNodeVO> roots = new ArrayList<>();
        for (WbsNodeVO node : flatList) {
            node.setChildren(new ArrayList<>());
            nodeMap.put(node.getId(), node);
        }
        for (WbsNodeVO node : flatList) {
            if (node.getParentId() == null) {
                roots.add(node);
                continue;
            }
            WbsNodeVO parent = nodeMap.get(node.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    @Transactional
    public WbsNodeVO create(Long projectId, CreateWbsNodeDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        LocalDateTime now = LocalDateTime.now();
        WbsNodeEntity entity = new WbsNodeEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setParentId(dto.getParentId());
        entity.setNodeCode(dto.getCode());
        entity.setNodeName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setOwnerId(dto.getOwnerId());
        entity.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        entity.setLevelNo(resolveLevel(projectId, dto.getParentId()));
        entity.setCreatedBy(UserContextHolder.getUserId());
        entity.setCreatedAt(now);
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        wbsMapper.insert(entity);
        return wbsMapper.selectById(entity.getId());
    }

    @Transactional
    public WbsNodeVO update(Long projectId, Long id, UpdateWbsNodeDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        WbsNodeEntity entity = ensureNode(projectId, id);
        if (dto.getParentId() != null && id.equals(dto.getParentId())) {
            throw new IllegalArgumentException("WBS 节点不能设置自己为父节点");
        }
        entity.setParentId(dto.getParentId());
        entity.setNodeCode(dto.getCode());
        entity.setNodeName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setOwnerId(dto.getOwnerId());
        entity.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        entity.setLevelNo(resolveLevel(projectId, dto.getParentId()));
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(LocalDateTime.now());
        wbsMapper.update(entity);
        return wbsMapper.selectById(id);
    }

    @Transactional
    public void delete(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureNode(projectId, id);
        if (wbsMapper.countChildren(projectId, id) > 0) {
            throw new IllegalArgumentException("当前节点存在子节点，不能删除");
        }
        if (wbsMapper.countReferencedRequirements(projectId, id) > 0
                || wbsMapper.countReferencedTasks(projectId, id) > 0) {
            throw new IllegalArgumentException("当前 WBS 节点已被需求或任务引用，不能删除");
        }
        wbsMapper.softDelete(id, UserContextHolder.getUserId(), LocalDateTime.now());
    }

    private WbsNodeEntity ensureNode(Long projectId, Long id) {
        WbsNodeEntity entity = wbsMapper.selectEntityById(id);
        if (entity == null || !projectId.equals(entity.getProjectId())) {
            throw new IllegalArgumentException("WBS 节点不存在");
        }
        return entity;
    }

    private Integer resolveLevel(Long projectId, Long parentId) {
        if (parentId == null) {
            return 1;
        }
        WbsNodeEntity parent = ensureNode(projectId, parentId);
        return parent.getLevelNo() + 1;
    }
}
