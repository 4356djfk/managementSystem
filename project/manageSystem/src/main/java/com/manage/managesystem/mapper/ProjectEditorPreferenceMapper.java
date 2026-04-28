package com.manage.managesystem.mapper;

import com.manage.managesystem.entity.ProjectEditorPreferenceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectEditorPreferenceMapper {
    ProjectEditorPreferenceEntity selectByProjectId(@Param("projectId") Long projectId);

    int insert(ProjectEditorPreferenceEntity entity);

    int update(ProjectEditorPreferenceEntity entity);
}
