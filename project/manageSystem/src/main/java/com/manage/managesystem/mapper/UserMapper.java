package com.manage.managesystem.mapper;

import com.manage.managesystem.entity.UserEntity;
import com.manage.managesystem.vo.ProjectMemberCandidateVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {
    UserEntity selectById(@Param("id") Long id);

    UserEntity selectByUsername(@Param("username") String username);

    List<UserEntity> selectList(@Param("keyword") String keyword, @Param("status") String status);

    List<ProjectMemberCandidateVO> selectProjectMemberCandidates();

    int insert(UserEntity user);

    int update(UserEntity user);

    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("updatedBy") Long updatedBy,
                     @Param("updatedAt") LocalDateTime updatedAt);

    int updateLastLogin(@Param("id") Long id,
                        @Param("lastLoginAt") LocalDateTime lastLoginAt,
                        @Param("updatedAt") LocalDateTime updatedAt);
}
