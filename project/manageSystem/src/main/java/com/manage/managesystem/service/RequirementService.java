package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.CreateRequirementDto;
import com.manage.managesystem.dto.RequirementQueryDto;
import com.manage.managesystem.dto.UpdateRequirementDto;
import com.manage.managesystem.entity.RequirementEntity;
import com.manage.managesystem.enums.RequirementStatusEnum;
import com.manage.managesystem.mapper.RequirementMapper;
import com.manage.managesystem.vo.RequirementVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequirementService {
    private final RequirementMapper requirementMapper;
    private final ProjectPermissionService projectPermissionService;

    public RequirementService(RequirementMapper requirementMapper, ProjectPermissionService projectPermissionService) {
        this.requirementMapper = requirementMapper;
        this.projectPermissionService = projectPermissionService;
    }

    public PageResult<RequirementVO> list(Long projectId, RequirementQueryDto queryDto) {
        projectPermissionService.ensureProjectParticipant(projectId);
        List<RequirementVO> list = requirementMapper.selectByProjectId(projectId, queryDto);
        PageResult<RequirementVO> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setPage(queryDto.getPage() == null ? 1 : queryDto.getPage());
        pageResult.setPageSize(queryDto.getPageSize() == null ? list.size() : queryDto.getPageSize());
        pageResult.setTotal((long) list.size());
        return pageResult;
    }

    @Transactional
    public RequirementVO create(Long projectId, CreateRequirementDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        LocalDateTime now = LocalDateTime.now();
        RequirementEntity entity = new RequirementEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setWbsId(dto.getWbsId());
        entity.setRequirementCode("REQ" + entity.getId());
        entity.setTitle(dto.getTitle());
        entity.setRequirementType(dto.getRequirementType());
        entity.setPriority(dto.getPriority());
        entity.setStatus(normalizeStatus(dto.getStatus()));
        entity.setDescription(dto.getDescription());
        entity.setProposerId(dto.getProposerId() == null ? UserContextHolder.getUserId() : dto.getProposerId());
        entity.setCreatedBy(UserContextHolder.getUserId());
        entity.setCreatedAt(now);
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        requirementMapper.insert(entity);
        return requirementMapper.selectById(entity.getId());
    }

    @Transactional
    public RequirementVO update(Long projectId, Long id, UpdateRequirementDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        RequirementEntity entity = ensureRequirement(projectId, id);
        entity.setWbsId(dto.getWbsId());
        entity.setTitle(dto.getTitle());
        entity.setRequirementType(dto.getRequirementType());
        entity.setPriority(dto.getPriority());
        entity.setStatus(normalizeStatus(dto.getStatus()));
        entity.setDescription(dto.getDescription());
        entity.setProposerId(dto.getProposerId() == null ? entity.getProposerId() : dto.getProposerId());
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(LocalDateTime.now());
        requirementMapper.update(entity);
        return requirementMapper.selectById(id);
    }

    private RequirementEntity ensureRequirement(Long projectId, Long id) {
        RequirementEntity entity = requirementMapper.selectEntityById(id);
        if (entity == null || !projectId.equals(entity.getProjectId())) {
            throw new IllegalArgumentException("需求不存在");
        }
        return entity;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return RequirementStatusEnum.DRAFT.name();
        }
        try {
            return RequirementStatusEnum.valueOf(status).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("非法的需求状态");
        }
    }
}
