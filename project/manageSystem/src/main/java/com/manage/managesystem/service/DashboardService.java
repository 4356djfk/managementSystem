package com.manage.managesystem.service;

import com.manage.managesystem.mapper.DashboardMapper;
import com.manage.managesystem.vo.DashboardHomeVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DashboardService {
    private final DashboardMapper dashboardMapper;
    private final ProjectPermissionService projectPermissionService;

    public DashboardService(DashboardMapper dashboardMapper,
                            ProjectPermissionService projectPermissionService) {
        this.dashboardMapper = dashboardMapper;
        this.projectPermissionService = projectPermissionService;
    }

    public DashboardHomeVO home() {
        if (!projectPermissionService.hasCurrentUserBusinessRole()) {
            return buildEmptyHome();
        }
        Long currentUserId = projectPermissionService.requireCurrentUserId();
        DashboardHomeVO home = dashboardMapper.selectHome(currentUserId);
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
        home.setUpcomingMilestones(dashboardMapper.selectUpcomingMilestones(currentUserId));
        return home;
    }

    private DashboardHomeVO buildEmptyHome() {
        DashboardHomeVO home = new DashboardHomeVO();
        home.setTaskCompletionRate(BigDecimal.ZERO);
        home.setPlannedCost(BigDecimal.ZERO);
        home.setActualCost(BigDecimal.ZERO);
        home.setUpcomingMilestones(java.util.List.of());
        return home;
    }
}
