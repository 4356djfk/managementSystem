package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.CreateProjectTemplateDto;
import com.manage.managesystem.dto.UpdateProjectTemplateDto;
import com.manage.managesystem.entity.ProjectTemplateEntity;
import com.manage.managesystem.enums.ProjectTemplateStatusEnum;
import com.manage.managesystem.mapper.ProjectTemplateMapper;
import com.manage.managesystem.vo.ProjectTemplateVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectTemplateService {
    private final ProjectTemplateMapper projectTemplateMapper;

    public ProjectTemplateService(ProjectTemplateMapper projectTemplateMapper) {
        this.projectTemplateMapper = projectTemplateMapper;
    }

    public PageResult<ProjectTemplateVO> list(String type, String status, Integer page, Integer pageSize) {
        List<ProjectTemplateEntity> entities = projectTemplateMapper.selectList(type, status);
        List<ProjectTemplateVO> list = new ArrayList<>();
        for (ProjectTemplateEntity entity : entities) {
            list.add(toVo(entity));
        }
        PageResult<ProjectTemplateVO> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setPage(page == null ? 1 : page);
        pageResult.setPageSize(pageSize == null ? list.size() : pageSize);
        pageResult.setTotal((long) list.size());
        return pageResult;
    }

    public ProjectTemplateVO detail(Long id) {
        ProjectTemplateEntity entity = projectTemplateMapper.selectById(id);
        if (entity == null) {
            throw new IllegalArgumentException("项目模板不存在");
        }
        return toVo(entity);
    }

    @Transactional
    public ProjectTemplateVO create(CreateProjectTemplateDto dto) {
        LocalDateTime now = LocalDateTime.now();
        ProjectTemplateEntity entity = new ProjectTemplateEntity();
        entity.setId(IdWorker.getId());
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setDescription(dto.getDescription());
        entity.setIsSystem(dto.getIsSystem() == null ? 0 : dto.getIsSystem());
        entity.setStatus(normalizeStatus(dto.getStatus()));
        entity.setCreatedBy(UserContextHolder.getUserId());
        entity.setCreatedAt(now);
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(now);
        projectTemplateMapper.insert(entity);
        return detail(entity.getId());
    }

    @Transactional
    public ProjectTemplateVO update(Long id, UpdateProjectTemplateDto dto) {
        ProjectTemplateEntity entity = projectTemplateMapper.selectById(id);
        if (entity == null) {
            throw new IllegalArgumentException("项目模板不存在");
        }
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setDescription(dto.getDescription());
        entity.setIsSystem(dto.getIsSystem() == null ? entity.getIsSystem() : dto.getIsSystem());
        entity.setStatus(normalizeStatus(dto.getStatus()));
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(LocalDateTime.now());
        projectTemplateMapper.update(entity);
        return detail(id);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return ProjectTemplateStatusEnum.ENABLED.name();
        }
        try {
            return ProjectTemplateStatusEnum.valueOf(status).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("非法的模板状态");
        }
    }

    private ProjectTemplateVO toVo(ProjectTemplateEntity entity) {
        ProjectTemplateVO vo = new ProjectTemplateVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setType(entity.getType());
        vo.setDescription(entity.getDescription());
        vo.setIsSystem(entity.getIsSystem());
        vo.setStatus(entity.getStatus());
        return vo;
    }
}
