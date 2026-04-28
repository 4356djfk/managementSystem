package com.manage.managesystem.mapper;

import com.manage.managesystem.vo.DashboardHomeVO;
import com.manage.managesystem.vo.MilestoneVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DashboardMapper {
    DashboardHomeVO selectHome(@Param("userId") Long userId);

    List<MilestoneVO> selectUpcomingMilestones(@Param("userId") Long userId);
}
