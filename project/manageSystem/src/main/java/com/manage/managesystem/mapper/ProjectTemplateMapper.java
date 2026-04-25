package com.manage.managesystem.mapper;

import com.manage.managesystem.entity.ProjectTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectTemplateMapper {
    List<ProjectTemplateEntity> selectList(@Param("type") String type, @Param("status") String status);

    ProjectTemplateEntity selectById(@Param("id") Long id);

    ProjectTemplateEntity selectFirstEnabled();

    int insert(ProjectTemplateEntity entity);

    int update(ProjectTemplateEntity entity);
}
