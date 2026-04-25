package com.manage.managesystem.mapper;

import com.manage.managesystem.vo.ProjectMemberVO;
import com.manage.managesystem.entity.ProjectMemberEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectMemberMapper {
    List<ProjectMemberVO> selectByProjectId(@Param("projectId") Long projectId);

    ProjectMemberVO selectById(@Param("id") Long id);

    ProjectMemberEntity selectEntityById(@Param("id") Long id);

    int insert(ProjectMemberEntity entity);

    int update(ProjectMemberEntity entity);

    int countActiveManagers(@Param("projectId") Long projectId);

    int countActiveMemberByProjectAndUser(@Param("projectId") Long projectId,
                                          @Param("userId") Long userId);
}
