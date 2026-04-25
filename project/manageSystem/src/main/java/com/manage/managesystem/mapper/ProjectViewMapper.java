package com.manage.managesystem.mapper;

import com.manage.managesystem.vo.CalendarEventVO;
import com.manage.managesystem.vo.ClosureCheckVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectViewMapper {
    List<CalendarEventVO> selectProjectCalendar(@Param("projectId") Long projectId);

    List<CalendarEventVO> selectMyCalendar(@Param("userId") Long userId);

    ClosureCheckVO selectClosureCheck(@Param("projectId") Long projectId);
}
