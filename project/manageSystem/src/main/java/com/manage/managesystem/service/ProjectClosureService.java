package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.dto.ChangeRequestQueryDto;
import com.manage.managesystem.dto.CreateArchiveItemDto;
import com.manage.managesystem.dto.CreateLessonLearnedDto;
import com.manage.managesystem.dto.GenerateReportDto;
import com.manage.managesystem.dto.RiskQueryDto;
import com.manage.managesystem.dto.TimesheetQueryDto;
import com.manage.managesystem.dto.UpdateLessonLearnedDto;
import com.manage.managesystem.entity.ArchiveItemEntity;
import com.manage.managesystem.entity.AttachmentEntity;
import com.manage.managesystem.entity.LessonLearnedEntity;
import com.manage.managesystem.entity.ReportRecordEntity;
import com.manage.managesystem.mapper.ChangeRequestMapper;
import com.manage.managesystem.mapper.CostMapper;
import com.manage.managesystem.mapper.MilestoneMapper;
import com.manage.managesystem.mapper.OpsMapper;
import com.manage.managesystem.mapper.ProjectClosureMapper;
import com.manage.managesystem.mapper.ProjectMapper;
import com.manage.managesystem.mapper.RiskMapper;
import com.manage.managesystem.mapper.ScopeBaselineMapper;
import com.manage.managesystem.mapper.TaskMapper;
import com.manage.managesystem.mapper.TimesheetMapper;
import com.manage.managesystem.vo.ArchiveItemVO;
import com.manage.managesystem.vo.AttachmentVO;
import com.manage.managesystem.vo.ChangeRequestVO;
import com.manage.managesystem.vo.CostActualVO;
import com.manage.managesystem.vo.CostBaselineVO;
import com.manage.managesystem.vo.CostPlanVO;
import com.manage.managesystem.vo.EvmMetricVO;
import com.manage.managesystem.vo.LessonLearnedVO;
import com.manage.managesystem.vo.MilestoneVO;
import com.manage.managesystem.vo.ProjectDashboardVO;
import com.manage.managesystem.vo.ProjectDetailVO;
import com.manage.managesystem.vo.ReportVO;
import com.manage.managesystem.vo.RiskVO;
import com.manage.managesystem.vo.ScopeBaselineVO;
import com.manage.managesystem.vo.TaskDependencyVO;
import com.manage.managesystem.vo.TaskListItemVO;
import com.manage.managesystem.vo.TimesheetVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ProjectClosureService {
    private final ProjectClosureMapper projectClosureMapper;
    private final ProjectMapper projectMapper;
    private final TaskMapper taskMapper;
    private final RiskMapper riskMapper;
    private final ChangeRequestMapper changeRequestMapper;
    private final TimesheetMapper timesheetMapper;
    private final CostMapper costMapper;
    private final MilestoneMapper milestoneMapper;
    private final ScopeBaselineMapper scopeBaselineMapper;
    private final OpsMapper opsMapper;
    private final ObjectMapper objectMapper;
    private final ProjectPermissionService projectPermissionService;

    public ProjectClosureService(ProjectClosureMapper projectClosureMapper,
                                 ProjectMapper projectMapper,
                                 TaskMapper taskMapper,
                                 RiskMapper riskMapper,
                                 ChangeRequestMapper changeRequestMapper,
                                 TimesheetMapper timesheetMapper,
                                 CostMapper costMapper,
                                 MilestoneMapper milestoneMapper,
                                 ScopeBaselineMapper scopeBaselineMapper,
                                 OpsMapper opsMapper,
                                 ObjectMapper objectMapper,
                                 ProjectPermissionService projectPermissionService) {
        this.projectClosureMapper = projectClosureMapper;
        this.projectMapper = projectMapper;
        this.taskMapper = taskMapper;
        this.riskMapper = riskMapper;
        this.changeRequestMapper = changeRequestMapper;
        this.timesheetMapper = timesheetMapper;
        this.costMapper = costMapper;
        this.milestoneMapper = milestoneMapper;
        this.scopeBaselineMapper = scopeBaselineMapper;
        this.opsMapper = opsMapper;
        this.objectMapper = objectMapper;
        this.projectPermissionService = projectPermissionService;
    }

    public List<ArchiveItemVO> listArchives(Long projectId) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProject(projectId);
        return projectClosureMapper.selectArchivesByProjectId(projectId);
    }

    @Transactional
    public ArchiveItemVO createArchive(Long projectId, CreateArchiveItemDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        LocalDateTime now = LocalDateTime.now();
        ArchiveItemEntity entity = new ArchiveItemEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setArchiveType(dto.getArchiveType());
        entity.setItemName(dto.getItemName());
        entity.setAttachmentId(dto.getAttachmentId());
        entity.setRepositoryUrl(dto.getRepositoryUrl());
        entity.setStatus(dto.getStatus() == null || dto.getStatus().isBlank() ? "ARCHIVED" : dto.getStatus());
        entity.setArchivedBy(UserContextHolder.getUserId());
        entity.setArchivedAt(now);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        projectClosureMapper.insertArchive(entity);
        return projectClosureMapper.selectArchivesByProjectId(projectId).stream()
                .filter(item -> item.getId().equals(entity.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("archive create failed"));
    }

    @Transactional
    public void deleteArchive(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        boolean exists = projectClosureMapper.selectArchivesByProjectId(projectId).stream()
                .anyMatch(item -> item.getId().equals(id));
        if (!exists) {
            throw new IllegalArgumentException("archive item not found");
        }
        projectClosureMapper.softDeleteArchive(projectId, id, LocalDateTime.now());
    }

    public List<LessonLearnedVO> listLessons(Long projectId) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProject(projectId);
        return projectClosureMapper.selectLessonsByProjectId(projectId);
    }

    @Transactional
    public LessonLearnedVO createLesson(Long projectId, CreateLessonLearnedDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        LocalDateTime now = LocalDateTime.now();
        LessonLearnedEntity entity = new LessonLearnedEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setCategory(dto.getCategory());
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setAuthorId(UserContextHolder.getUserId());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        projectClosureMapper.insertLesson(entity);
        return requireLesson(projectId, entity.getId());
    }

    @Transactional
    public LessonLearnedVO updateLesson(Long projectId, Long id, UpdateLessonLearnedDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        LessonLearnedEntity entity = requireLessonEntity(projectId, id);
        entity.setCategory(dto.getCategory());
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setUpdatedAt(LocalDateTime.now());
        projectClosureMapper.updateLesson(entity);
        return requireLesson(projectId, id);
    }

    @Transactional
    public void deleteLesson(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        requireLessonEntity(projectId, id);
        projectClosureMapper.softDeleteLesson(projectId, id, LocalDateTime.now());
    }

    public List<ReportVO> listReports(Long projectId) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProject(projectId);
        return projectClosureMapper.selectReportsByProjectId(projectId);
    }

    @Transactional
    public void deleteReport(Long projectId, Long id) {
        projectPermissionService.ensureProjectOwner(projectId);
        ensureProject(projectId);
        requireReport(projectId, id);
        projectClosureMapper.softDeleteReport(projectId, id);
    }

    @Transactional
    public ReportVO generateReport(Long projectId, GenerateReportDto dto, String reportType) {
        projectPermissionService.ensureProjectOwner(projectId);
        ensureProject(projectId);
        ReportRecordEntity entity = new ReportRecordEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setReportType(reportType);
        entity.setTitle(buildTitle(reportType, dto));
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setContentJson(buildReportContent(projectId, dto, reportType));
        entity.setGeneratedBy(UserContextHolder.getUserId());
        entity.setGeneratedAt(LocalDateTime.now());
        entity.setStatus("GENERATED");
        entity.setDeleted(0);
        projectClosureMapper.insertReport(entity);
        return projectClosureMapper.selectReportById(projectId, entity.getId());
    }

    public AttachmentVO attachmentDetail(Long id) {
        AttachmentVO vo = projectClosureMapper.selectAttachmentById(id);
        if (vo == null) {
            throw new IllegalArgumentException("attachment not found");
        }
        return vo;
    }

    @Transactional
    public void deleteAttachment(Long id) {
        attachmentDetail(id);
        projectClosureMapper.softDeleteAttachment(id, LocalDateTime.now());
    }

    @Transactional
    public AttachmentVO uploadAttachment(Long projectId, MultipartFile file) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("attachment file is required");
        }

        String originalName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename().trim() : "attachment.bin";
        String extension = extractExtension(originalName);
        String storedName = extension.isEmpty()
                ? UUID.randomUUID().toString().replace("-", "")
                : UUID.randomUUID().toString().replace("-", "") + "." + extension;

        try {
            Path uploadDir = Path.of(System.getProperty("java.io.tmpdir"), "managerSystem-attachments", String.valueOf(projectId));
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(storedName);
            Files.write(filePath, file.getBytes());

            AttachmentEntity entity = new AttachmentEntity();
            entity.setId(IdWorker.getId());
            entity.setFileName(originalName);
            entity.setFileUrl(filePath.toAbsolutePath().toString());
            entity.setFileSize(file.getSize());
            entity.setFileType(extension.isEmpty() ? "bin" : extension);
            entity.setBizType("PROJECT_ARCHIVE");
            entity.setBizId(projectId);
            entity.setUploadedBy(UserContextHolder.getUserId());
            entity.setUploadedAt(LocalDateTime.now());
            entity.setDeleted(0);
            opsMapper.insertAttachment(entity);
            return attachmentDetail(entity.getId());
        } catch (IOException e) {
            throw new IllegalStateException("attachment upload failed", e);
        }
    }

    private void ensureProject(Long projectId) {
        if (projectMapper.selectEntityById(projectId) == null) {
            throw new IllegalArgumentException("project not found");
        }
    }

    private LessonLearnedVO requireLesson(Long projectId, Long id) {
        LessonLearnedVO vo = projectClosureMapper.selectLessonById(projectId, id);
        if (vo == null) {
            throw new IllegalArgumentException("lesson learned not found");
        }
        return vo;
    }

    private LessonLearnedEntity requireLessonEntity(Long projectId, Long id) {
        LessonLearnedEntity entity = projectClosureMapper.selectLessonEntityById(projectId, id);
        if (entity == null) {
            throw new IllegalArgumentException("lesson learned not found");
        }
        return entity;
    }

    private ReportVO requireReport(Long projectId, Long id) {
        ReportVO vo = projectClosureMapper.selectReportById(projectId, id);
        if (vo == null) {
            throw new IllegalArgumentException("report not found");
        }
        return vo;
    }

    private String buildTitle(String reportType, GenerateReportDto dto) {
        if ("SUMMARY".equals(reportType)) {
            return "\u9879\u76ee\u603b\u7ed3\u62a5\u544a";
        }
        String type = dto.getType() == null || dto.getType().isBlank() ? "WEEKLY" : dto.getType();
        return "MONTHLY".equals(type) ? "\u9879\u76ee\u6708\u62a5" : "\u9879\u76ee\u5468\u62a5";
    }

    private String extractExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index + 1).trim().toLowerCase();
    }

    private String buildReportContent(Long projectId, GenerateReportDto dto, String reportType) {
        try {
            LocalDateTime generatedAt = LocalDateTime.now();
            ProjectDetailVO project = projectMapper.selectDetailById(projectId);
            ProjectDashboardVO dashboard = projectMapper.selectDashboard(projectId);
            List<TaskListItemVO> allTasks = taskMapper.selectByProjectId(projectId, null);
            List<TaskListItemVO> criticalTasks = taskMapper.selectCriticalPath(projectId);
            List<TaskDependencyVO> taskDependencies = taskMapper.selectDependenciesByProjectId(projectId);
            List<MilestoneVO> upcomingMilestones = projectMapper.selectUpcomingMilestones(projectId);
            List<MilestoneVO> allMilestones = milestoneMapper.selectByProjectId(projectId);
            List<RiskVO> openRisks = riskMapper.selectByProjectId(projectId, new RiskQueryDto()).stream()
                    .filter(item -> !"CLOSED".equals(item.getStatus()))
                    .sorted(Comparator
                            .comparingInt((RiskVO item) -> safeInteger(item.getProbability()) * safeInteger(item.getImpact()))
                            .reversed()
                            .thenComparing(RiskVO::getIdentifiedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();
            List<ChangeRequestVO> pendingChanges = changeRequestMapper.selectByProjectId(projectId, new ChangeRequestQueryDto()).stream()
                    .filter(item -> "SUBMITTED".equals(item.getStatus()) || "UNDER_REVIEW".equals(item.getStatus()))
                    .toList();
            List<TimesheetVO> timesheetRecords = timesheetMapper.selectByProjectId(projectId, new TimesheetQueryDto());
            List<CostPlanVO> costPlans = costMapper.selectPlansByProjectId(projectId);
            List<CostActualVO> costActuals = costMapper.selectActualsByProjectId(projectId);
            List<CostBaselineVO> costBaselines = costMapper.selectBaselinesByProjectId(projectId, "COST");
            List<ScopeBaselineVO> scopeBaselines = scopeBaselineMapper.selectByProjectId(projectId);
            EvmMetricVO evmMetric = costMapper.selectEvmMetric(projectId);
            BigDecimal totalHours = defaultDecimal(timesheetMapper.sumHoursByProjectId(projectId));
            BigDecimal totalLaborCost = defaultDecimal(timesheetMapper.sumLaborCostByProjectId(projectId));
            Integer userCount = defaultInteger(timesheetMapper.countDistinctUsers(projectId));
            BigDecimal referencePlannedCost = resolveReferencePlannedCost(project, dashboard, costPlans);
            BigDecimal referenceActualCost = resolveReferenceActualCost(project, dashboard, costActuals, totalLaborCost);
            Map<Long, List<TaskDependencyVO>> predecessorMap = buildPredecessorMap(taskDependencies);

            Map<String, Object> content = new LinkedHashMap<>();
            content.put("version", 3);
            content.put("title", buildTitle(reportType, dto));
            content.put("reportType", reportType);
            content.put("periodType", resolvePeriodType(reportType, dto));
            content.put("generatedAt", generatedAt);
            content.put("generatedBy", buildGeneratedBySection());
            content.put("period", buildPeriodSection(dto));
            content.put("project", buildProjectSection(project));
            content.put("overview", buildOverviewSection(
                    project,
                    dashboard,
                    allTasks,
                    totalHours,
                    totalLaborCost,
                    userCount,
                    referencePlannedCost,
                    referenceActualCost
            ));
            content.put("dashboard", buildDashboardSection(
                    project,
                    dashboard,
                    allTasks,
                    criticalTasks,
                    allMilestones,
                    totalHours,
                    totalLaborCost,
                    userCount,
                    referencePlannedCost,
                    referenceActualCost,
                    openRisks,
                    pendingChanges,
                    generatedAt
            ));
            content.put("taskSummary", buildTaskSummarySection(allTasks, criticalTasks, taskDependencies, allMilestones, generatedAt));
            content.put("upcomingTasks", buildUpcomingTaskItems(allTasks, predecessorMap, generatedAt));
            content.put("upcomingMilestones", buildMilestoneItems(upcomingMilestones));
            content.put("milestoneReport", buildMilestoneReportItems(allMilestones, generatedAt));
            content.put("criticalTasks", buildTaskItems(criticalTasks));
            content.put("taskDetails", buildTaskDetailItems(allTasks, predecessorMap, criticalTasks, generatedAt));
            content.put("overdueTasks", buildOverdueTaskItems(allTasks, predecessorMap, criticalTasks, generatedAt));
            content.put("resourceOverview", buildResourceOverviewItems(timesheetRecords, allTasks));
            content.put("resourceAssignments", buildResourceAssignmentItems(timesheetRecords, allTasks));
            content.put("costOverview", buildCostOverviewSection(
                    project,
                    referencePlannedCost,
                    costPlans,
                    costActuals,
                    totalLaborCost,
                    costBaselines,
                    evmMetric
            ));
            content.put("costDetails", buildCostDetailItems(allTasks, costPlans, costActuals, timesheetRecords));
            content.put("costCashFlow", buildCostCashFlowItems(costActuals, timesheetRecords));
            content.put("scheduleComparison", buildScheduleComparisonItems(allTasks, criticalTasks, generatedAt));
            content.put("baselineSnapshots", buildBaselineSnapshotSection(scopeBaselines, costBaselines));
            content.put("evmOverview", buildEvmOverviewSection(project, referencePlannedCost, evmMetric));
            content.put("evmDetails", buildEvmDetailItems(allTasks, costPlans, costActuals, timesheetRecords));
            content.put("openRisks", buildRiskItems(openRisks));
            content.put("pendingChanges", buildChangeItems(pendingChanges));

            if ("SUMMARY".equals(reportType)) {
                content.put("archives", buildArchiveItems(projectClosureMapper.selectArchivesByProjectId(projectId)));
                content.put("lessonsLearned", buildLessonItems(projectClosureMapper.selectLessonsByProjectId(projectId)));
            }

            return objectMapper.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to generate report content", e);
        }
    }

    private String resolvePeriodType(String reportType, GenerateReportDto dto) {
        if ("SUMMARY".equals(reportType)) {
            return "SUMMARY";
        }
        return StringUtils.hasText(dto.getType()) ? dto.getType().trim() : "WEEKLY";
    }

    private Map<String, Object> buildGeneratedBySection() {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("id", UserContextHolder.getUserId());
        section.put("name", resolveCurrentUserName());
        return section;
    }

    private Map<String, Object> buildPeriodSection(GenerateReportDto dto) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("startDate", dto.getStartDate());
        section.put("endDate", dto.getEndDate());
        return section;
    }

    private Map<String, Object> buildProjectSection(ProjectDetailVO project) {
        Map<String, Object> section = new LinkedHashMap<>();
        if (project == null) {
            return section;
        }
        section.put("id", project.getId());
        section.put("projectCode", project.getProjectCode());
        section.put("name", project.getName());
        section.put("description", project.getDescription());
        section.put("status", project.getStatus());
        section.put("ownerId", project.getOwnerId());
        section.put("ownerName", project.getOwnerName());
        section.put("startDate", project.getStartDate());
        section.put("endDate", project.getEndDate());
        section.put("actualStartDate", project.getActualStartDate());
        section.put("actualEndDate", project.getActualEndDate());
        section.put("memberCount", defaultInteger(project.getMemberCount()));
        section.put("plannedBudget", defaultDecimal(project.getPlannedBudget()));
        section.put("actualCost", defaultDecimal(project.getActualCost()));
        return section;
    }

    private Map<String, Object> buildOverviewSection(ProjectDetailVO project,
                                                     ProjectDashboardVO dashboard,
                                                     List<TaskListItemVO> tasks,
                                                     BigDecimal totalHours,
                                                     BigDecimal totalLaborCost,
                                                     Integer userCount,
                                                     BigDecimal plannedCost,
                                                     BigDecimal actualCost) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("taskTotal", dashboard == null ? 0 : defaultInteger(dashboard.getTaskTotal()));
        section.put("taskCompleted", dashboard == null ? 0 : defaultInteger(dashboard.getTaskCompleted()));
        section.put("taskCompletionRate", dashboard == null ? BigDecimal.ZERO : defaultDecimal(dashboard.getTaskCompletionRate()));
        section.put("plannedCost", plannedCost);
        section.put("actualCost", actualCost);
        section.put("openRiskCount", dashboard == null ? 0 : defaultInteger(dashboard.getOpenRiskCount()));
        section.put("pendingChangeCount", dashboard == null ? 0 : defaultInteger(dashboard.getPendingChangeCount()));
        section.put("totalHours", totalHours);
        section.put("totalLaborCost", totalLaborCost);
        section.put("userCount", userCount);
        section.put("memberCount", project == null ? 0 : defaultInteger(project.getMemberCount()));
        section.put("todoCount", countTasksByStatus(tasks, "TODO"));
        section.put("inProgressCount", countTasksByStatus(tasks, "IN_PROGRESS"));
        section.put("blockedCount", countTasksByStatus(tasks, "BLOCKED"));
        section.put("doneCount", countTasksByStatus(tasks, "DONE"));
        return section;
    }

    private Map<String, Object> buildDashboardSection(ProjectDetailVO project,
                                                      ProjectDashboardVO dashboard,
                                                      List<TaskListItemVO> tasks,
                                                      List<TaskListItemVO> criticalTasks,
                                                      List<MilestoneVO> milestones,
                                                      BigDecimal totalHours,
                                                      BigDecimal totalLaborCost,
                                                      Integer userCount,
                                                      BigDecimal plannedCost,
                                                      BigDecimal actualCost,
                                                      List<RiskVO> openRisks,
                                                      List<ChangeRequestVO> pendingChanges,
                                                      LocalDateTime generatedAt) {
        BigDecimal plannedHours = sumTaskHours(tasks, true);
        BigDecimal actualHours = totalHours.compareTo(BigDecimal.ZERO) > 0 ? totalHours : sumTaskHours(tasks, false);
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("taskTotal", tasks.size());
        section.put("taskCompleted", countTasksByStatus(tasks, "DONE"));
        section.put("taskCompletionRate", dashboard == null
                ? percentage(BigDecimal.valueOf(countTasksByStatus(tasks, "DONE")), BigDecimal.valueOf(Math.max(tasks.size(), 1)))
                : defaultDecimal(dashboard.getTaskCompletionRate()));
        section.put("criticalTaskCount", criticalTasks.size());
        section.put("overdueTaskCount", countOverdueTasks(tasks, generatedAt));
        section.put("upcomingTaskCount", countUpcomingTasks(tasks, generatedAt));
        section.put("milestoneTotal", milestones.size());
        section.put("milestoneCompleted", countMilestonesByStatus(milestones, "REACHED"));
        section.put("milestoneDelayed", countMilestonesByStatus(milestones, "DELAYED"));
        section.put("plannedHours", plannedHours);
        section.put("actualHours", actualHours);
        section.put("remainingHours", maxZero(plannedHours.subtract(actualHours)));
        section.put("hoursCompletionRate", percentage(actualHours, plannedHours));
        section.put("plannedCost", plannedCost);
        section.put("actualCost", actualCost);
        section.put("remainingCost", maxZero(plannedCost.subtract(actualCost)));
        section.put("costVariance", defaultDecimal(actualCost.subtract(plannedCost)));
        section.put("budgetExecutionRate", percentage(actualCost, plannedCost));
        section.put("openRiskCount", openRisks.size());
        section.put("pendingChangeCount", pendingChanges.size());
        section.put("memberCount", project == null ? 0 : defaultInteger(project.getMemberCount()));
        section.put("activeUserCount", userCount);
        return section;
    }

    private Map<String, Object> buildTaskSummarySection(List<TaskListItemVO> tasks,
                                                        List<TaskListItemVO> criticalTasks,
                                                        List<TaskDependencyVO> taskDependencies,
                                                        List<MilestoneVO> milestones,
                                                        LocalDateTime generatedAt) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("taskTotal", tasks.size());
        section.put("todoCount", countTasksByStatus(tasks, "TODO"));
        section.put("inProgressCount", countTasksByStatus(tasks, "IN_PROGRESS"));
        section.put("blockedCount", countTasksByStatus(tasks, "BLOCKED"));
        section.put("doneCount", countTasksByStatus(tasks, "DONE"));
        section.put("completionRate", percentage(
                BigDecimal.valueOf(countTasksByStatus(tasks, "DONE")),
                BigDecimal.valueOf(Math.max(tasks.size(), 1))
        ));
        section.put("criticalTaskCount", criticalTasks.size());
        section.put("overdueTaskCount", countOverdueTasks(tasks, generatedAt));
        section.put("dependencyCount", taskDependencies.size());
        section.put("milestoneCount", milestones.size());
        section.put("plannedHours", sumTaskHours(tasks, true));
        section.put("actualHours", sumTaskHours(tasks, false));
        return section;
    }

    private List<Map<String, Object>> buildUpcomingTaskItems(List<TaskListItemVO> tasks,
                                                             Map<Long, List<TaskDependencyVO>> predecessorMap,
                                                             LocalDateTime generatedAt) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (TaskListItemVO item : tasks) {
            String alertType = resolveUpcomingAlertType(item, generatedAt);
            if (alertType == null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("taskCode", item.getTaskCode());
            row.put("name", item.getName());
            row.put("status", item.getStatus());
            row.put("progress", defaultDecimal(item.getProgress()));
            row.put("assigneeName", item.getAssigneeName());
            row.put("plannedStartDate", item.getPlannedStartDate());
            row.put("plannedEndDate", item.getPlannedEndDate());
            row.put("durationDays", calculateDurationDays(item.getPlannedStartDate(), item.getPlannedEndDate()));
            row.put("alertType", alertType);
            row.put("predecessorTasks", buildPredecessorTaskNames(predecessorMap.get(item.getId())));
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> buildMilestoneReportItems(List<MilestoneVO> milestones, LocalDateTime generatedAt) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (MilestoneVO item : milestones) {
            Map<String, Object> row = new LinkedHashMap<>();
            int delayDays = calculateMilestoneDelayDays(item, generatedAt);
            row.put("id", item.getId());
            row.put("name", item.getName());
            row.put("description", item.getDescription());
            row.put("plannedDate", item.getPlannedDate());
            row.put("actualDate", item.getActualDate());
            row.put("status", item.getStatus());
            row.put("ownerName", item.getOwnerName());
            row.put("delayDays", delayDays);
            row.put("isOverdue", delayDays > 0 && !"REACHED".equals(item.getStatus()));
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> buildTaskDetailItems(List<TaskListItemVO> tasks,
                                                           Map<Long, List<TaskDependencyVO>> predecessorMap,
                                                           List<TaskListItemVO> criticalTasks,
                                                           LocalDateTime generatedAt) {
        Set<Long> criticalTaskIds = collectTaskIds(criticalTasks);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (TaskListItemVO item : tasks) {
            rows.add(buildTaskDetailRow(item, predecessorMap.get(item.getId()), criticalTaskIds, generatedAt));
        }
        return rows;
    }

    private List<Map<String, Object>> buildOverdueTaskItems(List<TaskListItemVO> tasks,
                                                            Map<Long, List<TaskDependencyVO>> predecessorMap,
                                                            List<TaskListItemVO> criticalTasks,
                                                            LocalDateTime generatedAt) {
        Set<Long> criticalTaskIds = collectTaskIds(criticalTasks);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (TaskListItemVO item : tasks) {
            if (!isTaskOverdue(item, generatedAt)) {
                continue;
            }
            rows.add(buildTaskDetailRow(item, predecessorMap.get(item.getId()), criticalTaskIds, generatedAt));
        }
        return rows;
    }

    private List<Map<String, Object>> buildResourceOverviewItems(List<TimesheetVO> timesheetRecords,
                                                                 List<TaskListItemVO> tasks) {
        Map<Long, String> userNames = new LinkedHashMap<>();
        Map<Long, BigDecimal> plannedHoursByUser = new HashMap<>();
        Map<Long, BigDecimal> actualHoursByUser = new HashMap<>();
        Map<Long, BigDecimal> laborCostByUser = new HashMap<>();
        Map<Long, Integer> assignedTaskCountByUser = new HashMap<>();
        Map<String, BigDecimal> userDayHours = new HashMap<>();
        Set<String> countedAssignments = new HashSet<>();

        for (TaskListItemVO task : tasks) {
            if (task.getAssigneeId() == null) {
                continue;
            }
            userNames.putIfAbsent(task.getAssigneeId(), task.getAssigneeName());
            addDecimal(plannedHoursByUser, task.getAssigneeId(), task.getPlannedHours());
            String assignmentKey = task.getAssigneeId() + ":" + task.getId();
            if (countedAssignments.add(assignmentKey)) {
                incrementInteger(assignedTaskCountByUser, task.getAssigneeId());
            }
        }

        for (TimesheetVO record : timesheetRecords) {
            if (record.getUserId() == null) {
                continue;
            }
            userNames.putIfAbsent(record.getUserId(), record.getUserName());
            addDecimal(actualHoursByUser, record.getUserId(), record.getHours());
            addDecimal(laborCostByUser, record.getUserId(), record.getLaborCost());
            if (record.getWorkDate() != null) {
                String dayKey = record.getUserId() + ":" + record.getWorkDate();
                userDayHours.put(dayKey, defaultDecimal(userDayHours.get(dayKey)).add(defaultDecimal(record.getHours())));
            }
        }

        Map<Long, Integer> overloadDaysByUser = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : userDayHours.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.valueOf(8)) <= 0) {
                continue;
            }
            Long userId = parseLeadingLong(entry.getKey());
            if (userId != null) {
                incrementInteger(overloadDaysByUser, userId);
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<Long, String> entry : userNames.entrySet()) {
            Long userId = entry.getKey();
            BigDecimal plannedHours = defaultDecimal(plannedHoursByUser.get(userId));
            BigDecimal actualHours = defaultDecimal(actualHoursByUser.get(userId));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("userId", userId);
            row.put("userName", entry.getValue());
            row.put("assignedTaskCount", assignedTaskCountByUser.getOrDefault(userId, 0));
            row.put("plannedHours", plannedHours);
            row.put("actualHours", actualHours);
            row.put("remainingHours", maxZero(plannedHours.subtract(actualHours)));
            row.put("utilizationRate", percentage(actualHours, plannedHours));
            row.put("laborCost", defaultDecimal(laborCostByUser.get(userId)));
            row.put("overloadDays", overloadDaysByUser.getOrDefault(userId, 0));
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> buildResourceAssignmentItems(List<TimesheetVO> timesheetRecords,
                                                                   List<TaskListItemVO> tasks) {
        Map<Long, TaskListItemVO> taskMap = buildTaskMap(tasks);
        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();

        for (TimesheetVO record : timesheetRecords) {
            if (record.getUserId() == null || record.getTaskId() == null) {
                continue;
            }
            String key = record.getUserId() + ":" + record.getTaskId();
            Map<String, Object> row = grouped.computeIfAbsent(key, ignored -> {
                Map<String, Object> item = new LinkedHashMap<>();
                TaskListItemVO task = taskMap.get(record.getTaskId());
                BigDecimal plannedHours = task == null ? BigDecimal.ZERO : defaultDecimal(task.getPlannedHours());
                item.put("userId", record.getUserId());
                item.put("userName", record.getUserName());
                item.put("taskId", record.getTaskId());
                item.put("taskName", record.getTaskName());
                item.put("plannedHours", plannedHours);
                item.put("actualHours", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
                item.put("remainingHours", plannedHours);
                item.put("laborCost", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
                item.put("status", task == null ? null : task.getStatus());
                item.put("progress", task == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : defaultDecimal(task.getProgress()));
                item.put("plannedStartDate", task == null ? null : task.getPlannedStartDate());
                item.put("plannedEndDate", task == null ? null : task.getPlannedEndDate());
                return item;
            });
            BigDecimal actualHours = defaultDecimal((BigDecimal) row.get("actualHours")).add(defaultDecimal(record.getHours()));
            BigDecimal plannedHours = defaultDecimal((BigDecimal) row.get("plannedHours"));
            row.put("actualHours", defaultDecimal(actualHours));
            row.put("laborCost", defaultDecimal(defaultDecimal((BigDecimal) row.get("laborCost")).add(defaultDecimal(record.getLaborCost()))));
            row.put("remainingHours", maxZero(plannedHours.subtract(actualHours)));
        }

        return new ArrayList<>(grouped.values());
    }

    private Map<String, Object> buildCostOverviewSection(ProjectDetailVO project,
                                                         BigDecimal plannedCost,
                                                         List<CostPlanVO> costPlans,
                                                         List<CostActualVO> costActuals,
                                                         BigDecimal totalLaborCost,
                                                         List<CostBaselineVO> costBaselines,
                                                         EvmMetricVO evmMetric) {
        BigDecimal manualActualCost = sumCostActuals(costActuals);
        BigDecimal actualCost = defaultDecimal(manualActualCost.add(totalLaborCost));
        BigDecimal baselineCost = costBaselines.isEmpty() ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : extractCostBaselinePlannedAmount(costBaselines.get(0).getSnapshotJson());
        BigDecimal budgetCost = project == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : defaultDecimal(project.getPlannedBudget());

        Map<String, Object> section = new LinkedHashMap<>();
        section.put("budgetCost", budgetCost);
        section.put("plannedCost", plannedCost);
        section.put("manualActualCost", manualActualCost);
        section.put("laborCost", totalLaborCost);
        section.put("actualCost", actualCost);
        section.put("remainingCost", maxZero(plannedCost.subtract(actualCost)));
        section.put("costVariance", defaultDecimal(actualCost.subtract(plannedCost)));
        section.put("baselineCost", baselineCost);
        section.put("baselineVariance", defaultDecimal(actualCost.subtract(baselineCost)));
        section.put("budgetExecutionRate", percentage(actualCost, plannedCost.compareTo(BigDecimal.ZERO) > 0 ? plannedCost : budgetCost));
        section.put("planItemCount", costPlans.size());
        section.put("actualRecordCount", costActuals.size());
        section.put("baselineCount", costBaselines.size());
        section.put("cpi", scaleDecimal(evmMetric == null ? BigDecimal.ZERO : evmMetric.getCpi(), 4));
        return section;
    }

    private List<Map<String, Object>> buildCostDetailItems(List<TaskListItemVO> tasks,
                                                           List<CostPlanVO> costPlans,
                                                           List<CostActualVO> costActuals,
                                                           List<TimesheetVO> timesheetRecords) {
        Map<Long, TaskListItemVO> taskMap = buildTaskMap(tasks);
        Map<Long, BigDecimal> plannedCostByTask = new HashMap<>();
        Map<Long, BigDecimal> manualActualCostByTask = new HashMap<>();
        Map<Long, BigDecimal> laborCostByTask = new HashMap<>();
        List<Long> orderedTaskIds = new ArrayList<>();
        Set<Long> seenTaskIds = new HashSet<>();

        for (TaskListItemVO task : tasks) {
            appendUniqueTaskId(orderedTaskIds, seenTaskIds, task.getId());
        }
        for (CostPlanVO item : costPlans) {
            if (item.getTaskId() != null) {
                addDecimal(plannedCostByTask, item.getTaskId(), item.getPlannedAmount());
                appendUniqueTaskId(orderedTaskIds, seenTaskIds, item.getTaskId());
            }
        }
        for (CostActualVO item : costActuals) {
            if (item.getTaskId() != null) {
                addDecimal(manualActualCostByTask, item.getTaskId(), item.getAmount());
                appendUniqueTaskId(orderedTaskIds, seenTaskIds, item.getTaskId());
            }
        }
        for (TimesheetVO item : timesheetRecords) {
            if (item.getTaskId() != null) {
                addDecimal(laborCostByTask, item.getTaskId(), item.getLaborCost());
                appendUniqueTaskId(orderedTaskIds, seenTaskIds, item.getTaskId());
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Long taskId : orderedTaskIds) {
            BigDecimal plannedCost = defaultDecimal(plannedCostByTask.get(taskId));
            BigDecimal manualActualCost = defaultDecimal(manualActualCostByTask.get(taskId));
            BigDecimal laborCost = defaultDecimal(laborCostByTask.get(taskId));
            BigDecimal actualCost = defaultDecimal(manualActualCost.add(laborCost));
            if (plannedCost.compareTo(BigDecimal.ZERO) <= 0 && actualCost.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            TaskListItemVO task = taskMap.get(taskId);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("taskId", taskId);
            row.put("taskCode", task == null ? null : task.getTaskCode());
            row.put("taskName", task == null ? null : task.getName());
            row.put("assigneeName", task == null ? null : task.getAssigneeName());
            row.put("status", task == null ? null : task.getStatus());
            row.put("progress", task == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : defaultDecimal(task.getProgress()));
            row.put("plannedCost", plannedCost);
            row.put("manualActualCost", manualActualCost);
            row.put("laborCost", laborCost);
            row.put("actualCost", actualCost);
            row.put("remainingCost", maxZero(plannedCost.subtract(actualCost)));
            row.put("costVariance", defaultDecimal(actualCost.subtract(plannedCost)));
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> buildCostCashFlowItems(List<CostActualVO> costActuals,
                                                             List<TimesheetVO> timesheetRecords) {
        Map<YearMonth, BigDecimal> manualActualByPeriod = new HashMap<>();
        Map<YearMonth, BigDecimal> laborCostByPeriod = new HashMap<>();
        List<YearMonth> periods = new ArrayList<>();
        Set<YearMonth> seenPeriods = new HashSet<>();

        for (CostActualVO item : costActuals) {
            if (item.getSpendDate() == null) {
                continue;
            }
            YearMonth period = YearMonth.from(item.getSpendDate());
            manualActualByPeriod.put(period, defaultDecimal(manualActualByPeriod.get(period)).add(defaultDecimal(item.getAmount())));
            if (seenPeriods.add(period)) {
                periods.add(period);
            }
        }
        for (TimesheetVO item : timesheetRecords) {
            if (item.getWorkDate() == null) {
                continue;
            }
            YearMonth period = YearMonth.from(item.getWorkDate());
            laborCostByPeriod.put(period, defaultDecimal(laborCostByPeriod.get(period)).add(defaultDecimal(item.getLaborCost())));
            if (seenPeriods.add(period)) {
                periods.add(period);
            }
        }

        periods.sort(Comparator.naturalOrder());
        BigDecimal cumulativeActualCost = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (YearMonth period : periods) {
            BigDecimal manualActualCost = defaultDecimal(manualActualByPeriod.get(period));
            BigDecimal laborCost = defaultDecimal(laborCostByPeriod.get(period));
            BigDecimal actualCost = defaultDecimal(manualActualCost.add(laborCost));
            cumulativeActualCost = defaultDecimal(cumulativeActualCost.add(actualCost));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("period", period.toString());
            row.put("manualActualCost", manualActualCost);
            row.put("laborCost", laborCost);
            row.put("actualCost", actualCost);
            row.put("cumulativeActualCost", cumulativeActualCost);
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> buildScheduleComparisonItems(List<TaskListItemVO> tasks,
                                                                   List<TaskListItemVO> criticalTasks,
                                                                   LocalDateTime generatedAt) {
        Set<Long> criticalTaskIds = collectTaskIds(criticalTasks);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (TaskListItemVO item : tasks) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("taskCode", item.getTaskCode());
            row.put("name", item.getName());
            row.put("status", item.getStatus());
            row.put("progress", defaultDecimal(item.getProgress()));
            row.put("plannedStartDate", item.getPlannedStartDate());
            row.put("plannedEndDate", item.getPlannedEndDate());
            row.put("actualStartDate", item.getActualStartDate());
            row.put("actualEndDate", item.getActualEndDate());
            row.put("plannedHours", defaultDecimal(item.getPlannedHours()));
            row.put("actualHours", defaultDecimal(item.getActualHours()));
            row.put("startVarianceDays", calculateStartVarianceDays(item, generatedAt));
            row.put("finishVarianceDays", calculateFinishVarianceDays(item, generatedAt));
            row.put("delayDays", calculateDelayDays(item, generatedAt));
            row.put("varianceStatus", resolveTaskVarianceStatus(item, generatedAt));
            row.put("isCritical", criticalTaskIds.contains(item.getId()));
            rows.add(row);
        }
        return rows;
    }

    private Map<String, Object> buildBaselineSnapshotSection(List<ScopeBaselineVO> scopeBaselines,
                                                             List<CostBaselineVO> costBaselines) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("scopeBaselineCount", scopeBaselines.size());
        section.put("costBaselineCount", costBaselines.size());

        if (!scopeBaselines.isEmpty()) {
            ScopeBaselineVO latestScopeBaseline = scopeBaselines.get(0);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", latestScopeBaseline.getId());
            row.put("versionNo", latestScopeBaseline.getVersionNo());
            row.put("baselineName", latestScopeBaseline.getBaselineName());
            row.put("status", latestScopeBaseline.getStatus());
            row.put("publishedAt", latestScopeBaseline.getPublishedAt());
            row.put("taskCount", extractScopeBaselineTaskCount(latestScopeBaseline.getSnapshotJson()));
            section.put("latestScopeBaseline", row);
        }

        if (!costBaselines.isEmpty()) {
            CostBaselineVO latestCostBaseline = costBaselines.get(0);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", latestCostBaseline.getId());
            row.put("baselineName", latestCostBaseline.getBaselineName());
            row.put("publishedAt", latestCostBaseline.getPublishedAt());
            row.put("plannedCost", extractCostBaselinePlannedAmount(latestCostBaseline.getSnapshotJson()));
            section.put("latestCostBaseline", row);
        }

        return section;
    }

    private Map<String, Object> buildEvmOverviewSection(ProjectDetailVO project,
                                                        BigDecimal plannedCost,
                                                        EvmMetricVO evmMetric) {
        BigDecimal pv = defaultDecimal(evmMetric == null ? BigDecimal.ZERO : evmMetric.getPv());
        BigDecimal ev = defaultDecimal(evmMetric == null ? BigDecimal.ZERO : evmMetric.getEv());
        BigDecimal ac = defaultDecimal(evmMetric == null ? BigDecimal.ZERO : evmMetric.getAc());
        BigDecimal cv = defaultDecimal(evmMetric == null ? BigDecimal.ZERO : evmMetric.getCv());
        BigDecimal sv = defaultDecimal(evmMetric == null ? BigDecimal.ZERO : evmMetric.getSv());
        BigDecimal cpi = scaleDecimal(evmMetric == null ? BigDecimal.ZERO : evmMetric.getCpi(), 4);
        BigDecimal spi = scaleDecimal(evmMetric == null ? BigDecimal.ZERO : evmMetric.getSpi(), 4);
        BigDecimal bac = plannedCost.compareTo(BigDecimal.ZERO) > 0
                ? plannedCost
                : (project == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : defaultDecimal(project.getPlannedBudget()));
        BigDecimal eac = cpi.compareTo(BigDecimal.ZERO) > 0 ? defaultDecimal(bac.divide(cpi, 2, RoundingMode.HALF_UP)) : ac;
        BigDecimal etc = defaultDecimal(eac.subtract(ac));
        BigDecimal vac = defaultDecimal(bac.subtract(eac));

        Map<String, Object> section = new LinkedHashMap<>();
        section.put("pv", pv);
        section.put("ev", ev);
        section.put("ac", ac);
        section.put("cv", cv);
        section.put("sv", sv);
        section.put("cpi", cpi);
        section.put("spi", spi);
        section.put("bac", bac);
        section.put("eac", eac);
        section.put("etc", etc);
        section.put("vac", vac);
        return section;
    }

    private List<Map<String, Object>> buildEvmDetailItems(List<TaskListItemVO> tasks,
                                                          List<CostPlanVO> costPlans,
                                                          List<CostActualVO> costActuals,
                                                          List<TimesheetVO> timesheetRecords) {
        Map<Long, TaskListItemVO> taskMap = buildTaskMap(tasks);
        Map<Long, BigDecimal> plannedCostByTask = new HashMap<>();
        Map<Long, BigDecimal> manualActualCostByTask = new HashMap<>();
        Map<Long, BigDecimal> laborCostByTask = new HashMap<>();
        List<Long> orderedTaskIds = new ArrayList<>();
        Set<Long> seenTaskIds = new HashSet<>();

        for (TaskListItemVO task : tasks) {
            appendUniqueTaskId(orderedTaskIds, seenTaskIds, task.getId());
        }
        for (CostPlanVO item : costPlans) {
            if (item.getTaskId() != null) {
                addDecimal(plannedCostByTask, item.getTaskId(), item.getPlannedAmount());
                appendUniqueTaskId(orderedTaskIds, seenTaskIds, item.getTaskId());
            }
        }
        for (CostActualVO item : costActuals) {
            if (item.getTaskId() != null) {
                addDecimal(manualActualCostByTask, item.getTaskId(), item.getAmount());
                appendUniqueTaskId(orderedTaskIds, seenTaskIds, item.getTaskId());
            }
        }
        for (TimesheetVO item : timesheetRecords) {
            if (item.getTaskId() != null) {
                addDecimal(laborCostByTask, item.getTaskId(), item.getLaborCost());
                appendUniqueTaskId(orderedTaskIds, seenTaskIds, item.getTaskId());
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Long taskId : orderedTaskIds) {
            TaskListItemVO task = taskMap.get(taskId);
            BigDecimal pv = defaultDecimal(plannedCostByTask.get(taskId));
            BigDecimal ac = defaultDecimal(defaultDecimal(manualActualCostByTask.get(taskId)).add(defaultDecimal(laborCostByTask.get(taskId))));
            BigDecimal progress = task == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : defaultDecimal(task.getProgress());
            BigDecimal ev = defaultDecimal(pv.multiply(progress).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            if (pv.compareTo(BigDecimal.ZERO) <= 0 && ac.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("taskId", taskId);
            row.put("taskCode", task == null ? null : task.getTaskCode());
            row.put("taskName", task == null ? null : task.getName());
            row.put("status", task == null ? null : task.getStatus());
            row.put("progress", progress);
            row.put("pv", pv);
            row.put("ev", ev);
            row.put("ac", ac);
            row.put("cv", defaultDecimal(ev.subtract(ac)));
            row.put("sv", defaultDecimal(ev.subtract(pv)));
            row.put("cpi", scaleDecimal(divideSafely(ev, ac, 4), 4));
            row.put("spi", scaleDecimal(divideSafely(ev, pv, 4), 4));
            rows.add(row);
        }
        return rows;
    }

    private BigDecimal resolveReferencePlannedCost(ProjectDetailVO project,
                                                   ProjectDashboardVO dashboard,
                                                   List<CostPlanVO> costPlans) {
        BigDecimal plannedCost = sumCostPlans(costPlans);
        if (plannedCost.compareTo(BigDecimal.ZERO) > 0) {
            return plannedCost;
        }
        if (project != null && project.getPlannedBudget() != null && project.getPlannedBudget().compareTo(BigDecimal.ZERO) > 0) {
            return defaultDecimal(project.getPlannedBudget());
        }
        if (dashboard != null && dashboard.getPlannedCost() != null && dashboard.getPlannedCost().compareTo(BigDecimal.ZERO) > 0) {
            return defaultDecimal(dashboard.getPlannedCost());
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveReferenceActualCost(ProjectDetailVO project,
                                                  ProjectDashboardVO dashboard,
                                                  List<CostActualVO> costActuals,
                                                  BigDecimal totalLaborCost) {
        BigDecimal actualCost = defaultDecimal(sumCostActuals(costActuals).add(totalLaborCost));
        if (actualCost.compareTo(BigDecimal.ZERO) > 0) {
            return actualCost;
        }
        if (project != null && project.getActualCost() != null && project.getActualCost().compareTo(BigDecimal.ZERO) > 0) {
            return defaultDecimal(project.getActualCost());
        }
        if (dashboard != null && dashboard.getActualCost() != null && dashboard.getActualCost().compareTo(BigDecimal.ZERO) > 0) {
            return defaultDecimal(dashboard.getActualCost());
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private Map<Long, List<TaskDependencyVO>> buildPredecessorMap(List<TaskDependencyVO> taskDependencies) {
        Map<Long, List<TaskDependencyVO>> predecessorMap = new HashMap<>();
        for (TaskDependencyVO item : taskDependencies) {
            if (item.getSuccessorTaskId() == null) {
                continue;
            }
            predecessorMap.computeIfAbsent(item.getSuccessorTaskId(), ignored -> new ArrayList<>()).add(item);
        }
        return predecessorMap;
    }

    private Map<String, Object> buildTaskDetailRow(TaskListItemVO item,
                                                   List<TaskDependencyVO> predecessors,
                                                   Set<Long> criticalTaskIds,
                                                   LocalDateTime generatedAt) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", item.getId());
        row.put("taskCode", item.getTaskCode());
        row.put("name", item.getName());
        row.put("taskType", item.getTaskType());
        row.put("status", item.getStatus());
        row.put("priority", item.getPriority());
        row.put("progress", defaultDecimal(item.getProgress()));
        row.put("assigneeName", item.getAssigneeName());
        row.put("plannedStartDate", item.getPlannedStartDate());
        row.put("plannedEndDate", item.getPlannedEndDate());
        row.put("actualStartDate", item.getActualStartDate());
        row.put("actualEndDate", item.getActualEndDate());
        row.put("plannedHours", defaultDecimal(item.getPlannedHours()));
        row.put("actualHours", defaultDecimal(item.getActualHours()));
        row.put("remainingHours", maxZero(defaultDecimal(item.getPlannedHours()).subtract(defaultDecimal(item.getActualHours()))));
        row.put("durationDays", calculateDurationDays(item.getPlannedStartDate(), item.getPlannedEndDate()));
        row.put("startVarianceDays", calculateStartVarianceDays(item, generatedAt));
        row.put("finishVarianceDays", calculateFinishVarianceDays(item, generatedAt));
        row.put("delayDays", calculateDelayDays(item, generatedAt));
        row.put("isOverdue", isTaskOverdue(item, generatedAt));
        row.put("isCritical", criticalTaskIds.contains(item.getId()));
        row.put("predecessorTasks", buildPredecessorTaskNames(predecessors));
        row.put("dependencyCount", predecessors == null ? 0 : predecessors.size());
        row.put("varianceStatus", resolveTaskVarianceStatus(item, generatedAt));
        row.put("remark", item.getRemark());
        row.put("description", item.getDescription());
        return row;
    }

    private Set<Long> collectTaskIds(List<TaskListItemVO> tasks) {
        Set<Long> ids = new HashSet<>();
        for (TaskListItemVO item : tasks) {
            if (item.getId() != null) {
                ids.add(item.getId());
            }
        }
        return ids;
    }

    private List<String> buildPredecessorTaskNames(List<TaskDependencyVO> predecessors) {
        List<String> names = new ArrayList<>();
        if (predecessors == null) {
            return names;
        }
        for (TaskDependencyVO item : predecessors) {
            if (StringUtils.hasText(item.getPredecessorTaskName())) {
                names.add(item.getPredecessorTaskName());
            }
        }
        return names;
    }

    private Map<Long, TaskListItemVO> buildTaskMap(List<TaskListItemVO> tasks) {
        Map<Long, TaskListItemVO> taskMap = new LinkedHashMap<>();
        for (TaskListItemVO item : tasks) {
            if (item.getId() != null) {
                taskMap.put(item.getId(), item);
            }
        }
        return taskMap;
    }

    private BigDecimal sumTaskHours(List<TaskListItemVO> tasks, boolean planned) {
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (TaskListItemVO item : tasks) {
            total = defaultDecimal(total.add(defaultDecimal(planned ? item.getPlannedHours() : item.getActualHours())));
        }
        return total;
    }

    private BigDecimal sumCostPlans(List<CostPlanVO> costPlans) {
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (CostPlanVO item : costPlans) {
            total = defaultDecimal(total.add(defaultDecimal(item.getPlannedAmount())));
        }
        return total;
    }

    private BigDecimal sumCostActuals(List<CostActualVO> costActuals) {
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (CostActualVO item : costActuals) {
            total = defaultDecimal(total.add(defaultDecimal(item.getAmount())));
        }
        return total;
    }

    private void addDecimal(Map<Long, BigDecimal> target, Long key, BigDecimal value) {
        if (key == null) {
            return;
        }
        target.put(key, defaultDecimal(target.get(key)).add(defaultDecimal(value)));
    }

    private void incrementInteger(Map<Long, Integer> target, Long key) {
        if (key == null) {
            return;
        }
        target.put(key, target.getOrDefault(key, 0) + 1);
    }

    private void appendUniqueTaskId(List<Long> orderedTaskIds, Set<Long> seenTaskIds, Long taskId) {
        if (taskId == null || !seenTaskIds.add(taskId)) {
            return;
        }
        orderedTaskIds.add(taskId);
    }

    private BigDecimal percentage(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal safeNumerator = numerator == null ? BigDecimal.ZERO : numerator;
        return safeNumerator.multiply(BigDecimal.valueOf(100)).divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal divideSafely(BigDecimal numerator, BigDecimal denominator, int scale) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP);
        }
        BigDecimal safeNumerator = numerator == null ? BigDecimal.ZERO : numerator;
        return safeNumerator.divide(denominator, scale, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleDecimal(BigDecimal value, int scale) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP);
        }
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    private BigDecimal maxZero(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return defaultDecimal(value);
    }

    private int countOverdueTasks(List<TaskListItemVO> tasks, LocalDateTime generatedAt) {
        int count = 0;
        for (TaskListItemVO item : tasks) {
            if (isTaskOverdue(item, generatedAt)) {
                count++;
            }
        }
        return count;
    }

    private int countUpcomingTasks(List<TaskListItemVO> tasks, LocalDateTime generatedAt) {
        int count = 0;
        for (TaskListItemVO item : tasks) {
            if (resolveUpcomingAlertType(item, generatedAt) != null) {
                count++;
            }
        }
        return count;
    }

    private int countMilestonesByStatus(List<MilestoneVO> milestones, String status) {
        int count = 0;
        for (MilestoneVO item : milestones) {
            if (status.equals(item.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private boolean isTaskOverdue(TaskListItemVO item, LocalDateTime generatedAt) {
        return calculateDelayDays(item, generatedAt) > 0 && !"DONE".equals(item.getStatus());
    }

    private String resolveUpcomingAlertType(TaskListItemVO item, LocalDateTime generatedAt) {
        if ("DONE".equals(item.getStatus())) {
            return null;
        }
        LocalDate today = generatedAt.toLocalDate();
        LocalDate nextWeek = today.plusDays(7);
        LocalDate plannedEndDate = toLocalDate(item.getPlannedEndDate());
        LocalDate plannedStartDate = toLocalDate(item.getPlannedStartDate());

        if (plannedEndDate != null && plannedEndDate.isBefore(today)) {
            return "已逾期";
        }
        if (plannedEndDate != null && !plannedEndDate.isAfter(nextWeek)) {
            return "即将到期";
        }
        if ("TODO".equals(item.getStatus())
                && plannedStartDate != null
                && !plannedStartDate.isBefore(today)
                && !plannedStartDate.isAfter(nextWeek)) {
            return "即将开始";
        }
        return null;
    }

    private int calculateDelayDays(TaskListItemVO item, LocalDateTime generatedAt) {
        LocalDate plannedEndDate = toLocalDate(item.getPlannedEndDate());
        if (plannedEndDate == null) {
            return 0;
        }
        if ("DONE".equals(item.getStatus()) && item.getActualEndDate() != null) {
            return Math.max((int) ChronoUnit.DAYS.between(plannedEndDate, item.getActualEndDate().toLocalDate()), 0);
        }
        return Math.max((int) ChronoUnit.DAYS.between(plannedEndDate, generatedAt.toLocalDate()), 0);
    }

    private int calculateStartVarianceDays(TaskListItemVO item, LocalDateTime generatedAt) {
        LocalDate plannedStartDate = toLocalDate(item.getPlannedStartDate());
        if (plannedStartDate == null) {
            return 0;
        }
        if (item.getActualStartDate() != null) {
            return Math.max((int) ChronoUnit.DAYS.between(plannedStartDate, item.getActualStartDate().toLocalDate()), 0);
        }
        if ("TODO".equals(item.getStatus())) {
            return Math.max((int) ChronoUnit.DAYS.between(plannedStartDate, generatedAt.toLocalDate()), 0);
        }
        return 0;
    }

    private int calculateFinishVarianceDays(TaskListItemVO item, LocalDateTime generatedAt) {
        return calculateDelayDays(item, generatedAt);
    }

    private String resolveTaskVarianceStatus(TaskListItemVO item, LocalDateTime generatedAt) {
        if ("BLOCKED".equals(item.getStatus())) {
            return "阻塞";
        }
        if (calculateDelayDays(item, generatedAt) > 0) {
            return "延期";
        }
        if ("DONE".equals(item.getStatus())
                && item.getPlannedEndDate() != null
                && item.getActualEndDate() != null
                && item.getActualEndDate().toLocalDate().isBefore(item.getPlannedEndDate().toLocalDate())) {
            return "提前";
        }
        return "正常";
    }

    private long calculateDurationDays(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return Math.max(ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate()) + 1, 0);
    }

    private int calculateMilestoneDelayDays(MilestoneVO item, LocalDateTime generatedAt) {
        LocalDate plannedDate = toLocalDate(item.getPlannedDate());
        if (plannedDate == null) {
            return 0;
        }
        if ("REACHED".equals(item.getStatus()) && item.getActualDate() != null) {
            return Math.max((int) ChronoUnit.DAYS.between(plannedDate, item.getActualDate().toLocalDate()), 0);
        }
        return Math.max((int) ChronoUnit.DAYS.between(plannedDate, generatedAt.toLocalDate()), 0);
    }

    private int extractScopeBaselineTaskCount(String snapshotJson) {
        if (!StringUtils.hasText(snapshotJson)) {
            return 0;
        }
        try {
            JsonNode root = objectMapper.readTree(snapshotJson);
            JsonNode tasks = root.path("tasks");
            return tasks.isArray() ? tasks.size() : 0;
        } catch (JsonProcessingException e) {
            return 0;
        }
    }

    private BigDecimal extractCostBaselinePlannedAmount(String snapshotJson) {
        if (!StringUtils.hasText(snapshotJson)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        try {
            JsonNode root = objectMapper.readTree(snapshotJson);
            JsonNode planned = root.path("totals").path("planned");
            if (planned.isNumber() || planned.isTextual()) {
                return defaultDecimal(new BigDecimal(planned.asText("0")));
            }
            BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            JsonNode plans = root.path("plans");
            if (plans.isArray()) {
                for (JsonNode plan : plans) {
                    JsonNode plannedAmount = plan.path("plannedAmount");
                    if (plannedAmount.isNumber() || plannedAmount.isTextual()) {
                        total = defaultDecimal(total.add(new BigDecimal(plannedAmount.asText("0"))));
                    }
                }
            }
            return total;
        } catch (JsonProcessingException e) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }

    private Long parseLeadingLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        int separator = value.indexOf(':');
        String raw = separator >= 0 ? value.substring(0, separator) : value;
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate toLocalDate(LocalDateTime value) {
        return value == null ? null : value.toLocalDate();
    }

    private List<Map<String, Object>> buildMilestoneItems(List<MilestoneVO> milestones) {
        return milestones.stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("name", item.getName());
            row.put("description", item.getDescription());
            row.put("plannedDate", item.getPlannedDate());
            row.put("actualDate", item.getActualDate());
            row.put("status", item.getStatus());
            row.put("ownerName", item.getOwnerName());
            return row;
        }).toList();
    }

    private List<Map<String, Object>> buildTaskItems(List<TaskListItemVO> tasks) {
        return tasks.stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("taskCode", item.getTaskCode());
            row.put("name", item.getName());
            row.put("status", item.getStatus());
            row.put("priority", item.getPriority());
            row.put("progress", defaultDecimal(item.getProgress()));
            row.put("assigneeName", item.getAssigneeName());
            row.put("plannedStartDate", item.getPlannedStartDate());
            row.put("plannedEndDate", item.getPlannedEndDate());
            row.put("actualStartDate", item.getActualStartDate());
            row.put("actualEndDate", item.getActualEndDate());
            row.put("plannedHours", defaultDecimal(item.getPlannedHours()));
            row.put("actualHours", defaultDecimal(item.getActualHours()));
            row.put("remainingHours", maxZero(defaultDecimal(item.getPlannedHours()).subtract(defaultDecimal(item.getActualHours()))));
            row.put("taskType", item.getTaskType());
            row.put("remark", item.getRemark());
            row.put("description", item.getDescription());
            return row;
        }).toList();
    }

    private List<Map<String, Object>> buildRiskItems(List<RiskVO> risks) {
        return risks.stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("riskCode", item.getRiskCode());
            row.put("name", item.getName());
            row.put("description", item.getDescription());
            row.put("level", item.getLevel());
            row.put("status", item.getStatus());
            row.put("probability", defaultInteger(item.getProbability()));
            row.put("impact", defaultInteger(item.getImpact()));
            row.put("responseStrategy", item.getResponseStrategy());
            row.put("taskName", item.getTaskName());
            row.put("ownerName", item.getOwnerName());
            row.put("identifiedAt", item.getIdentifiedAt());
            return row;
        }).toList();
    }

    private List<Map<String, Object>> buildChangeItems(List<ChangeRequestVO> changes) {
        return changes.stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("changeCode", item.getChangeCode());
            row.put("title", item.getTitle());
            row.put("changeType", item.getChangeType());
            row.put("priority", item.getPriority());
            row.put("status", item.getStatus());
            row.put("proposerName", item.getProposerName());
            row.put("approverName", item.getApproverName());
            row.put("submittedAt", item.getSubmittedAt());
            row.put("decidedAt", item.getDecidedAt());
            row.put("reason", item.getReason());
            row.put("impactAnalysis", item.getImpactAnalysis());
            row.put("decisionComment", item.getDecisionComment());
            return row;
        }).toList();
    }

    private List<Map<String, Object>> buildArchiveItems(List<ArchiveItemVO> archives) {
        return archives.stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("archiveType", item.getArchiveType());
            row.put("itemName", item.getItemName());
            row.put("status", item.getStatus());
            row.put("attachmentId", item.getAttachmentId());
            row.put("repositoryUrl", item.getRepositoryUrl());
            row.put("archivedAt", item.getArchivedAt());
            return row;
        }).toList();
    }

    private List<Map<String, Object>> buildLessonItems(List<LessonLearnedVO> lessons) {
        return lessons.stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("category", item.getCategory());
            row.put("title", item.getTitle());
            row.put("content", item.getContent());
            row.put("authorName", item.getAuthorName());
            row.put("createdAt", item.getCreatedAt());
            return row;
        }).toList();
    }

    private String resolveCurrentUserName() {
        if (UserContextHolder.get() == null) {
            return null;
        }
        if (StringUtils.hasText(UserContextHolder.get().getRealName())) {
            return UserContextHolder.get().getRealName();
        }
        return UserContextHolder.get().getUsername();
    }

    private int countTasksByStatus(List<TaskListItemVO> tasks, String status) {
        if ("IN_PROGRESS".equals(status)) {
            return (int) tasks.stream()
                    .filter(item -> "IN_PROGRESS".equals(item.getStatus()) || "PENDING_REVIEW".equals(item.getStatus()))
                    .count();
        }
        return (int) tasks.stream()
                .filter(item -> status.equals(item.getStatus()))
                .count();
    }

    private int safeInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private Integer defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
