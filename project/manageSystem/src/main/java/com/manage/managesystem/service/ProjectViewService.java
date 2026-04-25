package com.manage.managesystem.service;

import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.mapper.ProjectMapper;
import com.manage.managesystem.mapper.ProjectViewMapper;
import com.manage.managesystem.vo.CalendarEventVO;
import com.manage.managesystem.vo.ClosureCheckVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectViewService {
    private final ProjectViewMapper projectViewMapper;
    private final ProjectMapper projectMapper;

    public ProjectViewService(ProjectViewMapper projectViewMapper, ProjectMapper projectMapper) {
        this.projectViewMapper = projectViewMapper;
        this.projectMapper = projectMapper;
    }

    public List<CalendarEventVO> projectCalendar(Long projectId) {
        ensureProjectExists(projectId);
        return projectViewMapper.selectProjectCalendar(projectId);
    }

    public List<CalendarEventVO> myCalendar() {
        return projectViewMapper.selectMyCalendar(UserContextHolder.getUserId());
    }

    public ClosureCheckVO closureCheck(Long projectId) {
        ensureProjectExists(projectId);
        return projectViewMapper.selectClosureCheck(projectId);
    }

    private void ensureProjectExists(Long projectId) {
        if (projectMapper.selectEntityById(projectId) == null) {
            throw new IllegalArgumentException("project not found");
        }
    }
}
