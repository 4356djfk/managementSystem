package com.manage.managesystem.mapper;

import com.manage.managesystem.entity.WbsNodeEntity;
import com.manage.managesystem.vo.WbsNodeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WbsMapper {
    List<WbsNodeVO> selectByProjectId(@Param("projectId") Long projectId);

    WbsNodeVO selectById(@Param("id") Long id);

    WbsNodeEntity selectEntityById(@Param("id") Long id);

    int insert(WbsNodeEntity entity);

    int update(WbsNodeEntity entity);

    int softDelete(@Param("id") Long id,
                   @Param("updatedBy") Long updatedBy,
                   @Param("updatedAt") java.time.LocalDateTime updatedAt);

    int countChildren(@Param("projectId") Long projectId, @Param("parentId") Long parentId);

    int countReferencedRequirements(@Param("projectId") Long projectId, @Param("wbsId") Long wbsId);

    int countReferencedTasks(@Param("projectId") Long projectId, @Param("wbsId") Long wbsId);
}
