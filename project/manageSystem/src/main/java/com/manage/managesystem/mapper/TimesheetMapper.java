package com.manage.managesystem.mapper;

import com.manage.managesystem.dto.TimesheetQueryDto;
import com.manage.managesystem.entity.TimesheetEntity;
import com.manage.managesystem.vo.TimesheetVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface TimesheetMapper {
    List<TimesheetVO> selectByProjectId(@Param("projectId") Long projectId,
                                        @Param("query") TimesheetQueryDto query);

    TimesheetVO selectById(@Param("id") Long id);

    TimesheetEntity selectEntityById(@Param("id") Long id);

    int insert(TimesheetEntity entity);

    int update(TimesheetEntity entity);

    int softDelete(@Param("id") Long id,
                   @Param("updatedAt") java.time.LocalDateTime updatedAt);

    BigDecimal sumHoursByProjectId(@Param("projectId") Long projectId);

    BigDecimal sumHoursByTaskId(@Param("projectId") Long projectId, @Param("taskId") Long taskId);

    BigDecimal sumLaborCostByProjectId(@Param("projectId") Long projectId);

    BigDecimal sumLaborCostByTaskId(@Param("projectId") Long projectId, @Param("taskId") Long taskId);

    Integer countDistinctUsers(@Param("projectId") Long projectId);
}
