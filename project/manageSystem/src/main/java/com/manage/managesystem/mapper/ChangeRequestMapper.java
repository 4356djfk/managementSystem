package com.manage.managesystem.mapper;

import com.manage.managesystem.dto.ChangeRequestQueryDto;
import com.manage.managesystem.entity.ChangeRequestEntity;
import com.manage.managesystem.entity.ChangeRequestLogEntity;
import com.manage.managesystem.vo.ChangeRequestLogVO;
import com.manage.managesystem.vo.ChangeRequestVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChangeRequestMapper {
    List<ChangeRequestVO> selectByProjectId(@Param("projectId") Long projectId,
                                            @Param("query") ChangeRequestQueryDto query);

    ChangeRequestVO selectById(@Param("id") Long id);

    ChangeRequestEntity selectEntityById(@Param("id") Long id);

    int insert(ChangeRequestEntity entity);

    int updateApproval(@Param("id") Long id,
                       @Param("status") String status,
                       @Param("approverId") Long approverId,
                       @Param("decisionComment") String decisionComment,
                       @Param("decidedAt") java.time.LocalDateTime decidedAt,
                       @Param("updatedBy") Long updatedBy,
                       @Param("updatedAt") java.time.LocalDateTime updatedAt);

    int insertLog(ChangeRequestLogEntity entity);

    List<ChangeRequestLogVO> selectLogsByRequestId(@Param("changeRequestId") Long changeRequestId);
}
