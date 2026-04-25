package com.manage.managesystem.service;

import com.manage.managesystem.mapper.DashboardMapper;
import com.manage.managesystem.vo.DashboardHomeVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DashboardService {
    private final DashboardMapper dashboardMapper;

    public DashboardService(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    public DashboardHomeVO home() {
        DashboardHomeVO home = dashboardMapper.selectHome();
        if (home == null) {
            home = new DashboardHomeVO();
        }
        if (home.getTaskCompletionRate() == null) {
            home.setTaskCompletionRate(BigDecimal.ZERO);
        }
        if (home.getPlannedCost() == null) {
            home.setPlannedCost(BigDecimal.ZERO);
        }
        if (home.getActualCost() == null) {
            home.setActualCost(BigDecimal.ZERO);
        }
        home.setUpcomingMilestones(dashboardMapper.selectUpcomingMilestones());
        return home;
    }
}
