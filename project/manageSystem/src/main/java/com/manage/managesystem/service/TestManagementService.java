package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.CreateDefectDto;
import com.manage.managesystem.dto.CreateTestCaseDto;
import com.manage.managesystem.dto.CreateTestPlanDto;
import com.manage.managesystem.dto.DefectQueryDto;
import com.manage.managesystem.dto.GenerateReportDto;
import com.manage.managesystem.dto.TestCaseQueryDto;
import com.manage.managesystem.dto.TestPlanQueryDto;
import com.manage.managesystem.dto.UpdateDefectDto;
import com.manage.managesystem.dto.UpdateTestCaseDto;
import com.manage.managesystem.dto.UpdateTestPlanDto;
import com.manage.managesystem.entity.DefectEntity;
import com.manage.managesystem.entity.ReportRecordEntity;
import com.manage.managesystem.entity.RequirementEntity;
import com.manage.managesystem.entity.TestCaseEntity;
import com.manage.managesystem.entity.TestPlanEntity;
import com.manage.managesystem.entity.UserEntity;
import com.manage.managesystem.enums.DefectSeverityEnum;
import com.manage.managesystem.enums.DefectStatusEnum;
import com.manage.managesystem.enums.PriorityEnum;
import com.manage.managesystem.enums.TestCaseExecutionStatusEnum;
import com.manage.managesystem.enums.TestPlanStatusEnum;
import com.manage.managesystem.mapper.ProjectClosureMapper;
import com.manage.managesystem.mapper.ProjectMapper;
import com.manage.managesystem.mapper.RequirementMapper;
import com.manage.managesystem.mapper.TaskMapper;
import com.manage.managesystem.mapper.TestManagementMapper;
import com.manage.managesystem.mapper.UserMapper;
import com.manage.managesystem.vo.DefectVO;
import com.manage.managesystem.vo.ReportVO;
import com.manage.managesystem.vo.TestCaseVO;
import com.manage.managesystem.vo.TestPlanVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TestManagementService {
    private final TestManagementMapper testManagementMapper;
    private final ProjectMapper projectMapper;
    private final RequirementMapper requirementMapper;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final ProjectClosureMapper projectClosureMapper;
    private final ProjectPermissionService projectPermissionService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public TestManagementService(TestManagementMapper testManagementMapper,
                                 ProjectMapper projectMapper,
                                 RequirementMapper requirementMapper,
                                 TaskMapper taskMapper,
                                 UserMapper userMapper,
                                 ProjectClosureMapper projectClosureMapper,
                                 ProjectPermissionService projectPermissionService,
                                 NotificationService notificationService,
                                 ObjectMapper objectMapper) {
        this.testManagementMapper = testManagementMapper;
        this.projectMapper = projectMapper;
        this.requirementMapper = requirementMapper;
        this.taskMapper = taskMapper;
        this.userMapper = userMapper;
        this.projectClosureMapper = projectClosureMapper;
        this.projectPermissionService = projectPermissionService;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    public PageResult<TestPlanVO> listPlans(Long projectId, TestPlanQueryDto queryDto) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProject(projectId);
        return buildPageResult(testManagementMapper.selectPlansByProjectId(projectId, queryDto), queryDto.getPage(), queryDto.getPageSize());
    }

    @Transactional
    public TestPlanVO createPlan(Long projectId, CreateTestPlanDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        validateUser(dto.getOwnerId(), "test plan owner not found");
        LocalDateTime now = LocalDateTime.now();

        TestPlanEntity entity = new TestPlanEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setTitle(dto.getTitle().trim());
        entity.setVersionNo(normalizeText(dto.getVersionNo()));
        entity.setScopeDesc(normalizeText(dto.getScopeDesc()));
        entity.setOwnerId(dto.getOwnerId());
        entity.setStatus(normalizePlanStatus(dto.getStatus()));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        testManagementMapper.insertPlan(entity);
        return requirePlan(projectId, entity.getId());
    }

    @Transactional
    public TestPlanVO updatePlan(Long projectId, Long id, UpdateTestPlanDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        validateUser(dto.getOwnerId(), "test plan owner not found");
        TestPlanEntity entity = requirePlanEntity(projectId, id);
        entity.setTitle(dto.getTitle().trim());
        entity.setVersionNo(normalizeText(dto.getVersionNo()));
        entity.setScopeDesc(normalizeText(dto.getScopeDesc()));
        entity.setOwnerId(dto.getOwnerId());
        entity.setStatus(dto.getStatus() == null || dto.getStatus().isBlank()
                ? entity.getStatus()
                : normalizePlanStatus(dto.getStatus()));
        entity.setUpdatedAt(LocalDateTime.now());
        testManagementMapper.updatePlan(entity);
        return requirePlan(projectId, id);
    }

    @Transactional
    public void deletePlan(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        requirePlanEntity(projectId, id);
        if (testManagementMapper.countCasesByPlanId(projectId, id) > 0) {
            throw new IllegalArgumentException("test plan still has test cases");
        }
        testManagementMapper.softDeletePlan(projectId, id, LocalDateTime.now());
    }

    public PageResult<TestCaseVO> listCases(Long projectId, TestCaseQueryDto queryDto) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProject(projectId);
        return buildPageResult(testManagementMapper.selectCasesByProjectId(projectId, queryDto), queryDto.getPage(), queryDto.getPageSize());
    }

    @Transactional
    public TestCaseVO createCase(Long projectId, CreateTestCaseDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        validatePlan(projectId, dto.getTestPlanId());
        validateRequirement(projectId, dto.getRequirementId());
        validateTask(projectId, dto.getTaskId());
        validateUser(dto.getTesterId(), "tester not found");
        LocalDateTime now = LocalDateTime.now();

        TestCaseEntity entity = new TestCaseEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setTestPlanId(dto.getTestPlanId());
        entity.setRequirementId(dto.getRequirementId());
        entity.setTaskId(dto.getTaskId());
        entity.setCaseCode("TC" + entity.getId());
        entity.setTitle(dto.getTitle().trim());
        entity.setPrecondition(normalizeText(dto.getPrecondition()));
        entity.setSteps(normalizeText(dto.getSteps()));
        entity.setExpectedResult(normalizeText(dto.getExpectedResult()));
        entity.setActualResult(normalizeText(dto.getActualResult()));
        entity.setExecutionStatus(normalizeExecutionStatus(dto.getExecutionStatus()));
        entity.setTesterId(dto.getTesterId());
        entity.setExecutedAt(normalizeExecutedAt(entity.getExecutionStatus(), dto.getExecutedAt()));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        testManagementMapper.insertCase(entity);
        notificationService.notifyTestCaseAssigned(entity.getTesterId(), projectId, entity.getId(), entity.getTitle(), entity.getExecutionStatus());
        return requireCase(projectId, entity.getId());
    }

    @Transactional
    public TestCaseVO updateCase(Long projectId, Long id, UpdateTestCaseDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        validatePlan(projectId, dto.getTestPlanId());
        validateRequirement(projectId, dto.getRequirementId());
        validateTask(projectId, dto.getTaskId());
        validateUser(dto.getTesterId(), "tester not found");
        TestCaseEntity entity = requireCaseEntity(projectId, id);
        Long originalTesterId = entity.getTesterId();
        String originalStatus = entity.getExecutionStatus();
        entity.setTestPlanId(dto.getTestPlanId());
        entity.setRequirementId(dto.getRequirementId());
        entity.setTaskId(dto.getTaskId());
        entity.setTitle(dto.getTitle().trim());
        entity.setPrecondition(normalizeText(dto.getPrecondition()));
        entity.setSteps(normalizeText(dto.getSteps()));
        entity.setExpectedResult(normalizeText(dto.getExpectedResult()));
        entity.setActualResult(normalizeText(dto.getActualResult()));
        entity.setExecutionStatus(dto.getExecutionStatus() == null || dto.getExecutionStatus().isBlank()
                ? entity.getExecutionStatus()
                : normalizeExecutionStatus(dto.getExecutionStatus()));
        entity.setTesterId(dto.getTesterId());
        entity.setExecutedAt(normalizeExecutedAt(entity.getExecutionStatus(), dto.getExecutedAt()));
        entity.setUpdatedAt(LocalDateTime.now());
        testManagementMapper.updateCase(entity);
        if (!Objects.equals(originalTesterId, entity.getTesterId())) {
            notificationService.notifyTestCaseAssigned(entity.getTesterId(), projectId, entity.getId(), entity.getTitle(), entity.getExecutionStatus());
        } else if (entity.getTesterId() != null && !Objects.equals(originalStatus, entity.getExecutionStatus())) {
            notificationService.notifyTestCaseUpdated(entity.getTesterId(), projectId, entity.getId(), entity.getTitle(), entity.getExecutionStatus());
        }
        return requireCase(projectId, id);
    }

    @Transactional
    public void deleteCase(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        requireCaseEntity(projectId, id);
        if (testManagementMapper.countDefectsByCaseId(projectId, id) > 0) {
            throw new IllegalArgumentException("test case still has defects");
        }
        testManagementMapper.softDeleteCase(projectId, id, LocalDateTime.now());
    }

    public PageResult<DefectVO> listDefects(Long projectId, DefectQueryDto queryDto) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProject(projectId);
        return buildPageResult(testManagementMapper.selectDefectsByProjectId(projectId, queryDto), queryDto.getPage(), queryDto.getPageSize());
    }

    @Transactional
    public DefectVO createDefect(Long projectId, CreateDefectDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        DefectRelation relation = normalizeDefectRelation(projectId, dto.getTestCaseId(), dto.getRequirementId(), dto.getTaskId());
        validateUser(dto.getReporterId(), "defect reporter not found");
        validateUser(dto.getAssigneeId(), "defect assignee not found");
        LocalDateTime now = LocalDateTime.now();

        DefectEntity entity = new DefectEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setTestCaseId(relation.testCaseId());
        entity.setRequirementId(relation.requirementId());
        entity.setTaskId(relation.taskId());
        entity.setDefectCode("BUG" + entity.getId());
        entity.setTitle(dto.getTitle().trim());
        entity.setSeverity(normalizeDefectSeverity(dto.getSeverity()));
        entity.setPriority(normalizeDefectPriority(dto.getPriority()));
        entity.setStatus(normalizeDefectStatus(dto.getStatus()));
        entity.setReporterId(dto.getReporterId() == null ? UserContextHolder.getUserId() : dto.getReporterId());
        entity.setAssigneeId(dto.getAssigneeId());
        entity.setDescription(normalizeText(dto.getDescription()));
        entity.setResolution(normalizeText(dto.getResolution()));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        testManagementMapper.insertDefect(entity);
        notificationService.notifyDefectAssigned(entity.getAssigneeId(), projectId, entity.getId(), entity.getTitle(), entity.getSeverity(), entity.getStatus());
        return requireDefect(projectId, entity.getId());
    }

    @Transactional
    public DefectVO updateDefect(Long projectId, Long id, UpdateDefectDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        DefectRelation relation = normalizeDefectRelation(projectId, dto.getTestCaseId(), dto.getRequirementId(), dto.getTaskId());
        validateUser(dto.getReporterId(), "defect reporter not found");
        validateUser(dto.getAssigneeId(), "defect assignee not found");
        DefectEntity entity = requireDefectEntity(projectId, id);
        Long originalAssigneeId = entity.getAssigneeId();
        String originalStatus = entity.getStatus();
        String originalSeverity = entity.getSeverity();
        entity.setTestCaseId(relation.testCaseId());
        entity.setRequirementId(relation.requirementId());
        entity.setTaskId(relation.taskId());
        entity.setTitle(dto.getTitle().trim());
        entity.setSeverity(normalizeDefectSeverity(dto.getSeverity()));
        entity.setPriority(normalizeDefectPriority(dto.getPriority()));
        entity.setStatus(dto.getStatus() == null || dto.getStatus().isBlank()
                ? entity.getStatus()
                : normalizeDefectStatus(dto.getStatus()));
        entity.setReporterId(dto.getReporterId() == null ? entity.getReporterId() : dto.getReporterId());
        entity.setAssigneeId(dto.getAssigneeId());
        entity.setDescription(normalizeText(dto.getDescription()));
        entity.setResolution(normalizeText(dto.getResolution()));
        entity.setUpdatedAt(LocalDateTime.now());
        testManagementMapper.updateDefect(entity);
        if (!Objects.equals(originalAssigneeId, entity.getAssigneeId())) {
            notificationService.notifyDefectAssigned(entity.getAssigneeId(), projectId, entity.getId(), entity.getTitle(), entity.getSeverity(), entity.getStatus());
        } else if (entity.getAssigneeId() != null
                && (!Objects.equals(originalStatus, entity.getStatus()) || !Objects.equals(originalSeverity, entity.getSeverity()))) {
            notificationService.notifyDefectUpdated(entity.getAssigneeId(), projectId, entity.getId(), entity.getTitle(), entity.getSeverity(), entity.getStatus());
        }
        return requireDefect(projectId, id);
    }

    @Transactional
    public void deleteDefect(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        requireDefectEntity(projectId, id);
        testManagementMapper.softDeleteDefect(projectId, id, LocalDateTime.now());
    }

    public List<ReportVO> listTestReports(Long projectId) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProject(projectId);
        return projectClosureMapper.selectReportsByProjectId(projectId).stream()
                .filter(item -> "TEST".equals(item.getReportType()))
                .toList();
    }

    @Transactional
    public ReportVO generateTestReport(Long projectId, GenerateReportDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        ReportRecordEntity entity = new ReportRecordEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setReportType("TEST");
        entity.setTitle(buildTestReportTitle(dto));
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setContentJson(buildTestReportContent(projectId, dto));
        entity.setGeneratedBy(UserContextHolder.getUserId());
        entity.setGeneratedAt(LocalDateTime.now());
        entity.setStatus("GENERATED");
        entity.setDeleted(0);
        projectClosureMapper.insertReport(entity);
        return requireTestReport(projectId, entity.getId());
    }

    @Transactional
    public void deleteTestReport(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        requireTestReport(projectId, id);
        projectClosureMapper.softDeleteReport(projectId, id);
    }

    private void ensureProject(Long projectId) {
        if (projectMapper.selectEntityById(projectId) == null) {
            throw new IllegalArgumentException("project not found");
        }
    }

    private void validateUser(Long userId, String message) {
        if (userId == null) {
            return;
        }
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validatePlan(Long projectId, Long testPlanId) {
        if (testPlanId != null && testManagementMapper.selectPlanEntityById(projectId, testPlanId) == null) {
            throw new IllegalArgumentException("test plan not found");
        }
    }

    private void validateRequirement(Long projectId, Long requirementId) {
        if (requirementId == null) {
            return;
        }
        RequirementEntity requirement = requirementMapper.selectEntityById(requirementId);
        if (requirement == null || !projectId.equals(requirement.getProjectId())) {
            throw new IllegalArgumentException("requirement not found");
        }
    }

    private void validateTask(Long projectId, Long taskId) {
        if (taskId != null && taskMapper.selectEntityById(projectId, taskId) == null) {
            throw new IllegalArgumentException("task not found");
        }
    }

    private String normalizePlanStatus(String status) {
        if (status == null || status.isBlank()) {
            return TestPlanStatusEnum.DRAFT.name();
        }
        try {
            return TestPlanStatusEnum.valueOf(status.trim().toUpperCase()).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid test plan status");
        }
    }

    private String normalizeExecutionStatus(String status) {
        if (status == null || status.isBlank()) {
            return TestCaseExecutionStatusEnum.NOT_RUN.name();
        }
        try {
            return TestCaseExecutionStatusEnum.valueOf(status.trim().toUpperCase()).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid test case status");
        }
    }

    private String normalizeDefectStatus(String status) {
        if (status == null || status.isBlank()) {
            return DefectStatusEnum.NEW.name();
        }
        try {
            return DefectStatusEnum.valueOf(status.trim().toUpperCase()).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid defect status");
        }
    }

    private String normalizeDefectSeverity(String severity) {
        try {
            return DefectSeverityEnum.valueOf(severity.trim().toUpperCase()).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid defect severity");
        }
    }

    private String normalizeDefectPriority(String priority) {
        String normalized = priority.trim().toUpperCase();
        if (!PriorityEnum.LOW.name().equals(normalized)
                && !PriorityEnum.MEDIUM.name().equals(normalized)
                && !PriorityEnum.HIGH.name().equals(normalized)) {
            throw new IllegalArgumentException("invalid defect priority");
        }
        return normalized;
    }

    private LocalDateTime normalizeExecutedAt(String executionStatus, LocalDateTime executedAt) {
        if (TestCaseExecutionStatusEnum.NOT_RUN.name().equals(executionStatus)) {
            return null;
        }
        return executedAt == null ? LocalDateTime.now() : executedAt;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private TestPlanVO requirePlan(Long projectId, Long id) {
        TestPlanVO vo = testManagementMapper.selectPlanById(projectId, id);
        if (vo == null) {
            throw new IllegalArgumentException("test plan not found");
        }
        return vo;
    }

    private TestPlanEntity requirePlanEntity(Long projectId, Long id) {
        TestPlanEntity entity = testManagementMapper.selectPlanEntityById(projectId, id);
        if (entity == null) {
            throw new IllegalArgumentException("test plan not found");
        }
        return entity;
    }

    private TestCaseVO requireCase(Long projectId, Long id) {
        TestCaseVO vo = testManagementMapper.selectCaseById(projectId, id);
        if (vo == null) {
            throw new IllegalArgumentException("test case not found");
        }
        return vo;
    }

    private TestCaseEntity requireCaseEntity(Long projectId, Long id) {
        TestCaseEntity entity = testManagementMapper.selectCaseEntityById(projectId, id);
        if (entity == null) {
            throw new IllegalArgumentException("test case not found");
        }
        return entity;
    }

    private DefectVO requireDefect(Long projectId, Long id) {
        DefectVO vo = testManagementMapper.selectDefectById(projectId, id);
        if (vo == null) {
            throw new IllegalArgumentException("defect not found");
        }
        return vo;
    }

    private DefectEntity requireDefectEntity(Long projectId, Long id) {
        DefectEntity entity = testManagementMapper.selectDefectEntityById(projectId, id);
        if (entity == null) {
            throw new IllegalArgumentException("defect not found");
        }
        return entity;
    }

    private ReportVO requireTestReport(Long projectId, Long id) {
        ReportVO report = projectClosureMapper.selectReportById(projectId, id);
        if (report == null || !"TEST".equals(report.getReportType())) {
            throw new IllegalArgumentException("test report not found");
        }
        return report;
    }

    private String buildTestReportTitle(GenerateReportDto dto) {
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        if (startDate != null && endDate != null) {
            return "测试报告（%s 至 %s）".formatted(startDate, endDate);
        }
        if (startDate != null) {
            return "测试报告（自 %s 起）".formatted(startDate);
        }
        if (endDate != null) {
            return "测试报告（截至 %s）".formatted(endDate);
        }
        return "测试报告";
    }

    private String buildTestReportContent(Long projectId, GenerateReportDto dto) {
        try {
            List<TestPlanVO> plans = testManagementMapper.selectPlansByProjectId(projectId, null);
            List<TestCaseVO> cases = testManagementMapper.selectCasesByProjectId(projectId, null);
            List<DefectVO> defects = testManagementMapper.selectDefectsByProjectId(projectId, null);

            long executedCount = cases.stream()
                    .filter(item -> !TestCaseExecutionStatusEnum.NOT_RUN.name().equals(item.getExecutionStatus()))
                    .count();
            long passedCount = cases.stream()
                    .filter(item -> TestCaseExecutionStatusEnum.PASSED.name().equals(item.getExecutionStatus()))
                    .count();
            BigDecimal passRate = executedCount == 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(passedCount * 100.0 / executedCount).setScale(2, RoundingMode.HALF_UP);

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("planCount", plans.size());
            summary.put("caseCount", cases.size());
            summary.put("executedCaseCount", executedCount);
            summary.put("passedCaseCount", passedCount);
            summary.put("passRate", passRate);
            summary.put("openDefectCount", defects.stream().filter(item -> !DefectStatusEnum.CLOSED.name().equals(item.getStatus())).count());
            summary.put("generatedAt", LocalDateTime.now());
            Map<String, Object> dateRange = new LinkedHashMap<>();
            dateRange.put("startDate", dto.getStartDate());
            dateRange.put("endDate", dto.getEndDate());
            summary.put("dateRange", dateRange);

            Map<String, Long> planStatus = new LinkedHashMap<>();
            for (TestPlanStatusEnum status : TestPlanStatusEnum.values()) {
                planStatus.put(status.name(), plans.stream().filter(item -> status.name().equals(item.getStatus())).count());
            }

            Map<String, Long> caseStatus = new LinkedHashMap<>();
            for (TestCaseExecutionStatusEnum status : TestCaseExecutionStatusEnum.values()) {
                caseStatus.put(status.name(), cases.stream().filter(item -> status.name().equals(item.getExecutionStatus())).count());
            }

            Map<String, Long> defectStatus = new LinkedHashMap<>();
            for (DefectStatusEnum status : DefectStatusEnum.values()) {
                defectStatus.put(status.name(), defects.stream().filter(item -> status.name().equals(item.getStatus())).count());
            }

            Map<String, Long> defectSeverity = new LinkedHashMap<>();
            for (DefectSeverityEnum severity : DefectSeverityEnum.values()) {
                defectSeverity.put(severity.name(), defects.stream().filter(item -> severity.name().equals(item.getSeverity())).count());
            }

            Map<String, Object> content = new LinkedHashMap<>();
            content.put("summary", summary);
            content.put("planStatus", planStatus);
            content.put("caseStatus", caseStatus);
            content.put("defectStatus", defectStatus);
            content.put("defectSeverity", defectSeverity);
            content.put("recentFailedCases", cases.stream()
                    .filter(item -> TestCaseExecutionStatusEnum.FAILED.name().equals(item.getExecutionStatus())
                            || TestCaseExecutionStatusEnum.BLOCKED.name().equals(item.getExecutionStatus()))
                    .limit(10)
                    .toList());
            content.put("openDefects", defects.stream()
                    .filter(item -> !DefectStatusEnum.CLOSED.name().equals(item.getStatus()))
                    .limit(10)
                    .toList());
            return objectMapper.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to generate test report", e);
        }
    }

    private DefectRelation normalizeDefectRelation(Long projectId, Long testCaseId, Long requirementId, Long taskId) {
        validateRequirement(projectId, requirementId);
        validateTask(projectId, taskId);
        if (testCaseId == null) {
            return new DefectRelation(null, requirementId, taskId);
        }
        TestCaseEntity testCase = requireCaseEntity(projectId, testCaseId);
        Long normalizedRequirementId = requirementId == null ? testCase.getRequirementId() : requirementId;
        Long normalizedTaskId = taskId == null ? testCase.getTaskId() : taskId;
        if (requirementId != null && !Objects.equals(requirementId, testCase.getRequirementId())) {
            throw new IllegalArgumentException("defect requirement does not match test case");
        }
        if (taskId != null && !Objects.equals(taskId, testCase.getTaskId())) {
            throw new IllegalArgumentException("defect task does not match test case");
        }
        return new DefectRelation(testCaseId, normalizedRequirementId, normalizedTaskId);
    }

    private <T> PageResult<T> buildPageResult(List<T> list, Integer page, Integer pageSize) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setPage(page == null ? 1 : page);
        pageResult.setPageSize(pageSize == null ? list.size() : pageSize);
        pageResult.setTotal((long) list.size());
        return pageResult;
    }

    private record DefectRelation(Long testCaseId, Long requirementId, Long taskId) {
    }
}
