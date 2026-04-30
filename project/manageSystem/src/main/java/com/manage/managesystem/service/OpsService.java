package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.AuditLogQueryDto;
import com.manage.managesystem.dto.CreateAcceptanceItemDto;
import com.manage.managesystem.dto.ExportDataDto;
import com.manage.managesystem.dto.RiskQueryDto;
import com.manage.managesystem.dto.SearchQueryDto;
import com.manage.managesystem.dto.UpdateAcceptanceItemDto;
import com.manage.managesystem.entity.AttachmentEntity;
import com.manage.managesystem.entity.CostActualEntity;
import com.manage.managesystem.entity.CostPlanEntity;
import com.manage.managesystem.entity.ExportTaskEntity;
import com.manage.managesystem.entity.RiskEntity;
import com.manage.managesystem.entity.TaskEntity;
import com.manage.managesystem.entity.TimesheetEntity;
import com.manage.managesystem.enums.ExportFormatEnum;
import com.manage.managesystem.enums.ExportModuleEnum;
import com.manage.managesystem.enums.RiskStatusEnum;
import com.manage.managesystem.enums.TaskConstraintTypeEnum;
import com.manage.managesystem.enums.TaskStatusEnum;
import com.manage.managesystem.enums.TaskTypeEnum;
import com.manage.managesystem.mapper.CostMapper;
import com.manage.managesystem.mapper.OpsMapper;
import com.manage.managesystem.mapper.ProjectClosureMapper;
import com.manage.managesystem.mapper.ProjectMapper;
import com.manage.managesystem.mapper.RiskMapper;
import com.manage.managesystem.mapper.TaskMapper;
import com.manage.managesystem.mapper.TimesheetMapper;
import com.manage.managesystem.mapper.UserMapper;
import com.manage.managesystem.vo.AcceptanceItemVO;
import com.manage.managesystem.vo.AuditLogVO;
import com.manage.managesystem.vo.CostActualVO;
import com.manage.managesystem.vo.CostPlanVO;
import com.manage.managesystem.vo.ExportTaskVO;
import com.manage.managesystem.vo.ImportResultVO;
import com.manage.managesystem.vo.NotificationVO;
import com.manage.managesystem.vo.ReportVO;
import com.manage.managesystem.vo.SearchResultVO;
import com.manage.managesystem.vo.TimesheetVO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class OpsService {
    public record DownloadTemplate(String fileName, byte[] content) {}

    private static final float PDF_MARGIN = 40f;
    private static final float PDF_TITLE_FONT_SIZE = 16f;
    private static final float PDF_BODY_FONT_SIZE = 10.5f;
    private static final float PDF_META_FONT_SIZE = 9.5f;
    private static final float PDF_TABLE_FONT_SIZE = 8.5f;
    private static final float PDF_TABLE_LINE_HEIGHT = 11f;
    private static final float PDF_TABLE_CELL_PADDING = 4f;
    private static final float PDF_TABLE_MIN_COLUMN_WIDTH = 48f;
    private static final int PDF_TABLE_HEADER_MAX_LINES = 2;
    private static final int PDF_TABLE_BODY_MAX_LINES = 2;
    private static final float PDF_LINE_HEIGHT = 15f;
    private static final float PDF_SECTION_GAP = 8f;

    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
    );

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    private static final List<String> PROJECT_AUDIT_MODULES = List.of(
            "EDITOR_PREFERENCE",
            "PROJECT_MEMBER",
            "PROJECT_CHARTER",
            "WBS",
            "REQUIREMENT",
            "MILESTONE",
            "TASK",
            "RISK",
            "COST",
            "TIMESHEET",
            "CHANGE",
            "SCOPE_BASELINE",
            "ACCEPTANCE",
            "ARCHIVE",
            "LESSON",
            "REPORT",
            "CALENDAR",
            "CLOSURE",
            "PROJECT",
            "EXPORT",
            "IMPORT",
            "ATTACHMENT"
    );

    private final OpsMapper opsMapper;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;
    private final TaskMapper taskMapper;
    private final RiskMapper riskMapper;
    private final CostMapper costMapper;
    private final TimesheetMapper timesheetMapper;
    private final ProjectClosureMapper projectClosureMapper;
    private final ObjectMapper objectMapper;
    private final ProjectPermissionService projectPermissionService;
    private final TaskProgressRollupService taskProgressRollupService;
    private final NotificationService notificationService;

    private static final class PdfCursor implements AutoCloseable {
        private final PDDocument document;
        private final PDRectangle pageSize;
        private PDPage page;
        private PDPageContentStream stream;
        private float y;

        private PdfCursor(PDDocument document, PDRectangle pageSize) throws IOException {
            this.document = document;
            this.pageSize = pageSize;
            addPage();
        }

        private void addPage() throws IOException {
            closeStream();
            page = new PDPage(pageSize);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = page.getMediaBox().getHeight() - PDF_MARGIN;
        }

        private float maxWidth() {
            return page.getMediaBox().getWidth() - PDF_MARGIN * 2;
        }

        private void ensureSpace(float height) throws IOException {
            if (y - height < PDF_MARGIN) {
                addPage();
            }
        }

        private void closeStream() throws IOException {
            if (stream != null) {
                stream.close();
                stream = null;
            }
        }

        @Override
        public void close() throws IOException {
            closeStream();
        }
    }

    public OpsService(OpsMapper opsMapper,
                      ProjectMapper projectMapper,
                      UserMapper userMapper,
                      TaskMapper taskMapper,
                      RiskMapper riskMapper,
                      CostMapper costMapper,
                      TimesheetMapper timesheetMapper,
                      ProjectClosureMapper projectClosureMapper,
                      ObjectMapper objectMapper,
                      ProjectPermissionService projectPermissionService,
                      TaskProgressRollupService taskProgressRollupService,
                      NotificationService notificationService) {
        this.opsMapper = opsMapper;
        this.projectMapper = projectMapper;
        this.userMapper = userMapper;
        this.taskMapper = taskMapper;
        this.riskMapper = riskMapper;
        this.costMapper = costMapper;
        this.timesheetMapper = timesheetMapper;
        this.projectClosureMapper = projectClosureMapper;
        this.objectMapper = objectMapper;
        this.projectPermissionService = projectPermissionService;
        this.taskProgressRollupService = taskProgressRollupService;
        this.notificationService = notificationService;
    }

    public List<AcceptanceItemVO> listAcceptanceItems(Long projectId) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProject(projectId);
        return opsMapper.selectAcceptanceItems(projectId);
    }

    @Transactional
    public AcceptanceItemVO createAcceptanceItem(Long projectId, CreateAcceptanceItemDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        validateOwner(dto.getOwnerId());
        Long id = IdWorker.getId();
        LocalDateTime now = LocalDateTime.now();
        opsMapper.insertAcceptanceItem(
                id,
                projectId,
                dto.getItemName(),
                dto.getDescription(),
                defaultText(dto.getStatus(), "PENDING"),
                dto.getOwnerId(),
                isCompleted(dto.getStatus()) ? now : null,
                buildAcceptanceRemark(dto.getItemType(), dto.getAttachmentId()),
                now,
                now
        );
        notificationService.notifyAcceptanceItemAssigned(dto.getOwnerId(), projectId, id, dto.getItemName());
        if (isCompleted(dto.getStatus())) {
            notificationService.notifyAcceptanceItemCompleted(dto.getOwnerId(), projectId, id, dto.getItemName());
        }
        return requireAcceptanceItem(projectId, id);
    }

    @Transactional
    public AcceptanceItemVO updateAcceptanceItem(Long projectId, Long id, UpdateAcceptanceItemDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        validateOwner(dto.getOwnerId());
        AcceptanceItemVO existing = requireAcceptanceItem(projectId, id);
        opsMapper.updateAcceptanceItem(
                projectId,
                id,
                dto.getItemName(),
                dto.getDescription(),
                defaultText(dto.getStatus(), "PENDING"),
                dto.getOwnerId(),
                isCompleted(dto.getStatus()) ? LocalDateTime.now() : null,
                buildAcceptanceRemark(dto.getItemType(), dto.getAttachmentId()),
                LocalDateTime.now()
        );
        if (!Objects.equals(existing.getOwnerId(), dto.getOwnerId())) {
            notificationService.notifyAcceptanceItemAssigned(dto.getOwnerId(), projectId, id, dto.getItemName());
        }
        if (!isCompleted(existing.getStatus()) && isCompleted(dto.getStatus())) {
            notificationService.notifyAcceptanceItemCompleted(dto.getOwnerId(), projectId, id, dto.getItemName());
        }
        return requireAcceptanceItem(projectId, id);
    }

    @Transactional
    public void deleteAcceptanceItem(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        requireAcceptanceItem(projectId, id);
        opsMapper.softDeleteAcceptanceItem(projectId, id, LocalDateTime.now());
    }

    public PageResult<NotificationVO> listNotifications() {
        List<NotificationVO> list = opsMapper.selectNotifications(UserContextHolder.getUserId());
        PageResult<NotificationVO> result = new PageResult<>();
        result.setList(list);
        result.setPage(1);
        result.setPageSize(list.size());
        result.setTotal((long) list.size());
        return result;
    }

    @Transactional
    public void markRead(Long id) {
        opsMapper.markNotificationRead(id, UserContextHolder.getUserId(), LocalDateTime.now());
    }

    @Transactional
    public void markAllRead() {
        opsMapper.markAllNotificationsRead(UserContextHolder.getUserId(), LocalDateTime.now());
    }

    @Transactional
    public ExportTaskVO createExportTask(ExportDataDto dto) {
        Long projectId = dto.getProjectId();
        if (projectId == null) {
            throw new IllegalArgumentException("projectId is required for export");
        }
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProject(projectId);

        String module = normalizeUpperRequired(dto.getModule(), "module is required");
        String format = normalizeUpperRequired(dto.getFormat(), "format is required");
        parseExportModule(module);
        parseExportFormat(format);

        ExportTaskEntity entity = new ExportTaskEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setModuleName(module);
        entity.setExportFormat(format);
        entity.setFilterJson(writeJson(dto.getFilters()));
        entity.setStatus("PENDING");
        entity.setRequestedBy(UserContextHolder.getUserId());
        entity.setRequestedAt(LocalDateTime.now());
        opsMapper.insertExportTask(entity);

        AttachmentEntity attachment = generateExportAttachment(entity);
        opsMapper.insertAttachment(attachment);
        opsMapper.updateExportTaskResult(entity.getId(), "COMPLETED", attachment.getId(), LocalDateTime.now());
        notificationService.notifyExportCompleted(entity.getRequestedBy(), projectId, entity.getId(), entity.getModuleName(), entity.getExportFormat());
        return opsMapper.selectExportTaskById(entity.getId());
    }

    @Transactional
    public ImportResultVO importExcel(MultipartFile file, String module, Long projectId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("import file cannot be empty");
        }
        if (projectId == null) {
            throw new IllegalArgumentException("projectId is required for import");
        }
        projectPermissionService.ensureProjectOwner(projectId);
        ensureProject(projectId);

        String targetModule = normalizeUpperRequired(module, "module is required");
        parseExportModule(targetModule);

        try {
            List<Map<String, String>> rows = readImportRows(file);
            return switch (targetModule) {
                case "TASK" -> importTasks(projectId, rows);
                case "RISK" -> importRisks(projectId, rows);
                case "COST" -> importCosts(projectId, rows);
                case "TIMESHEET" -> importTimesheets(projectId, rows);
                default -> throw new IllegalArgumentException("import module is not supported yet");
            };
        } catch (IOException e) {
            throw new IllegalStateException("import file parse failed", e);
        }
    }

    public DownloadTemplate downloadImportTemplate(String module) {
        String targetModule = normalizeUpperRequired(module, "module is required");
        parseExportModule(targetModule);
        try {
            return new DownloadTemplate(buildTemplateFileName(targetModule), buildImportTemplateContent(targetModule));
        } catch (IOException e) {
            throw new IllegalStateException("template generate failed", e);
        }
    }

    public PageResult<SearchResultVO> search(SearchQueryDto query) {
        Long currentUserId = projectPermissionService.requireCurrentUserId();
        if (!projectPermissionService.hasCurrentUserBusinessRole()) {
            PageResult<SearchResultVO> empty = new PageResult<>();
            empty.setList(List.of());
            empty.setPage(query.getPage() == null ? 1 : query.getPage());
            empty.setPageSize(query.getPageSize() == null ? 0 : query.getPageSize());
            empty.setTotal(0L);
            return empty;
        }
        if (query.getProjectId() != null) {
            projectPermissionService.ensureProjectParticipant(query.getProjectId());
        }
        List<SearchResultVO> list = opsMapper.search(query.getKeyword(), query.getType(), query.getProjectId());
        list = list.stream()
                .filter(item -> projectPermissionService.canAccessProject(item.getProjectId(), currentUserId))
                .toList();
        PageResult<SearchResultVO> result = new PageResult<>();
        result.setList(list);
        result.setPage(query.getPage() == null ? 1 : query.getPage());
        result.setPageSize(query.getPageSize() == null ? list.size() : query.getPageSize());
        result.setTotal((long) list.size());
        return result;
    }

    public PageResult<AuditLogVO> auditLogs(AuditLogQueryDto query) {
        AuditLogQueryDto safeQuery = query == null ? new AuditLogQueryDto() : query;
        if (safeQuery.getProjectId() != null) {
            projectPermissionService.ensureProjectParticipant(safeQuery.getProjectId());
            if (!applyProjectAuditScope(safeQuery)) {
                return buildAuditLogPage(safeQuery, List.of(), 0);
            }
        } else {
            projectPermissionService.ensureSysAdmin();
        }
        List<AuditLogVO> list = opsMapper.selectAuditLogs(safeQuery);
        return buildAuditLogPage(safeQuery, list, list.size());
    }

    private AcceptanceItemVO requireAcceptanceItem(Long projectId, Long id) {
        AcceptanceItemVO vo = opsMapper.selectAcceptanceItemById(projectId, id);
        if (vo == null) {
            throw new IllegalArgumentException("acceptance item not found");
        }
        return vo;
    }

    private void ensureProject(Long projectId) {
        if (projectMapper.selectEntityById(projectId) == null) {
            throw new IllegalArgumentException("project not found");
        }
    }

    private boolean applyProjectAuditScope(AuditLogQueryDto query) {
        String module = normalizeAuditModule(query.getModule());
        if (module != null && !PROJECT_AUDIT_MODULES.contains(module)) {
            return false;
        }
        query.setModule(module);
        query.setAllowedModules(PROJECT_AUDIT_MODULES);
        query.setBusinessOperatorsOnly(Boolean.TRUE);
        return true;
    }

    private PageResult<AuditLogVO> buildAuditLogPage(AuditLogQueryDto query, List<AuditLogVO> list, int total) {
        PageResult<AuditLogVO> result = new PageResult<>();
        result.setList(list);
        result.setPage(query.getPage() == null ? 1 : query.getPage());
        result.setPageSize(query.getPageSize() == null ? list.size() : query.getPageSize());
        result.setTotal((long) total);
        return result;
    }

    private void validateOwner(Long ownerId) {
        if (ownerId != null && userMapper.selectById(ownerId) == null) {
            throw new IllegalArgumentException("owner not found: " + ownerId);
        }
    }

    private void validateUser(Long userId) {
        if (userId != null && userMapper.selectById(userId) == null) {
            throw new IllegalArgumentException("user not found: " + userId);
        }
    }

    private void validateTask(Long projectId, Long taskId) {
        if (taskId != null && taskMapper.selectEntityById(projectId, taskId) == null) {
            throw new IllegalArgumentException("task not found: " + taskId);
        }
    }

    private TaskEntity requireTask(Long projectId, Long taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("task not found");
        }
        TaskEntity task = taskMapper.selectEntityById(projectId, taskId);
        if (task == null) {
            throw new IllegalArgumentException("task not found");
        }
        return task;
    }

    private boolean isCompleted(String status) {
        return "COMPLETED".equalsIgnoreCase(defaultText(status, ""));
    }

    private String buildAcceptanceRemark(String itemType, Long attachmentId) {
        Map<String, Object> remark = new HashMap<>();
        remark.put("itemType", itemType);
        remark.put("attachmentId", attachmentId);
        return writeJson(remark);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("json serialize failed", e);
        }
    }

    private AttachmentEntity generateExportAttachment(ExportTaskEntity exportTask) {
        try {
            byte[] content = buildExportContent(exportTask);
            String fileName = buildExportFileName(exportTask);
            Path exportDir = Path.of(System.getProperty("java.io.tmpdir"), "managerSystem-exports");
            Files.createDirectories(exportDir);
            Path filePath = exportDir.resolve(fileName);
            Files.write(filePath, content);

            AttachmentEntity attachment = new AttachmentEntity();
            attachment.setId(IdWorker.getId());
            attachment.setFileName(fileName);
            attachment.setFileUrl(filePath.toAbsolutePath().toString());
            attachment.setFileSize((long) content.length);
            attachment.setFileType(resolveExportFileExtension(exportTask));
            attachment.setBizType("EXPORT_TASK");
            attachment.setBizId(exportTask.getId());
            attachment.setUploadedBy(UserContextHolder.getUserId());
            attachment.setUploadedAt(LocalDateTime.now());
            attachment.setDeleted(0);
            return attachment;
        } catch (IOException e) {
            throw new IllegalStateException("export file generate failed", e);
        }
    }

    private byte[] buildExportContent(ExportTaskEntity exportTask) throws IOException {
        if (exportTask.getProjectId() == null) {
            throw new IllegalArgumentException("projectId is required for export");
        }

        String format = normalizeUpperRequired(exportTask.getExportFormat(), "format is required");
        String module = normalizeUpperRequired(exportTask.getModuleName(), "module is required");
        return switch (format) {
            case "EXCEL" -> buildExcelExportContent(module, exportTask.getProjectId());
            case "PDF" -> buildPdfExportContent(module, exportTask.getProjectId());
            default -> throw new IllegalArgumentException("export format is not supported yet");
        };
    }

    private byte[] buildExcelExportContent(String module, Long projectId) throws IOException {
        try (Workbook workbook = createExportWorkbook(module, projectId)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            autoSizeColumns(sheet, headerRow == null ? 0 : Math.max(headerRow.getLastCellNum(), 0));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] buildPdfExportContent(String module, Long projectId) throws IOException {
        try (Workbook workbook = createExportWorkbook(module, projectId);
             PDDocument document = new PDDocument()) {
            PDFont font = loadPdfFont(document);
            List<List<String>> rows = extractSheetRows(workbook.getSheetAt(0));
            List<String> headers = rows.isEmpty() ? List.of() : rows.get(0);
            List<List<String>> dataRows = rows.size() > 1 ? rows.subList(1, rows.size()) : List.of();
            PDRectangle pageSize = resolvePdfPageSize(headers.size());

            try (PdfCursor cursor = new PdfCursor(document, pageSize)) {
                writePdfParagraph(cursor, font, PDF_TITLE_FONT_SIZE, "Project Export");
                writePdfParagraph(cursor, font, PDF_META_FONT_SIZE, "Module: " + resolveExportModuleLabel(module));
                writePdfParagraph(cursor, font, PDF_META_FONT_SIZE, "Project ID: " + safeCell(projectId));
                writePdfParagraph(cursor, font, PDF_META_FONT_SIZE, "Exported At: " + safeCell(LocalDateTime.now()));
                cursor.y -= PDF_SECTION_GAP;

                if (headers.isEmpty()) {
                    writePdfParagraph(cursor, font, PDF_META_FONT_SIZE, "No data available for export.");
                } else {
                    writePdfTable(cursor, font, headers, dataRows);
                    if (dataRows.isEmpty()) {
                        cursor.y -= PDF_SECTION_GAP;
                        writePdfParagraph(cursor, font, PDF_META_FONT_SIZE, "No data available for export.");
                    }
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private Workbook createExportWorkbook(String module, Long projectId) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(resolveExportSheetName(module));
        populateExportSheet(sheet, module, projectId);
        return workbook;
    }

    private void populateExportSheet(Sheet sheet, String module, Long projectId) {
        switch (module) {
            case "TASK" -> buildTaskExportSheet(sheet, projectId);
            case "RISK" -> buildRiskExportSheet(sheet, projectId);
            case "COST" -> buildCostExportSheet(sheet, projectId);
            case "TIMESHEET" -> buildTimesheetExportSheet(sheet, projectId);
            case "REPORT" -> buildReportExportSheet(sheet, projectId);
            default -> throw new IllegalArgumentException("export module is not supported yet");
        }
    }

    private String resolveExportSheetName(String module) {
        return switch (module) {
            case "TASK" -> "tasks";
            case "RISK" -> "risks";
            case "COST" -> "costs";
            case "TIMESHEET" -> "timesheets";
            case "REPORT" -> "reports";
            default -> "export";
        };
    }

    private String buildExportFileName(ExportTaskEntity exportTask) {
        String projectPart = exportTask.getProjectId() == null ? "global" : String.valueOf(exportTask.getProjectId());
        String modulePart = exportTask.getModuleName() == null ? "export" : exportTask.getModuleName().toLowerCase(Locale.ROOT);
        return String.format("%s_%s_%d.%s", modulePart, projectPart, exportTask.getId(), resolveExportFileExtension(exportTask));
    }

    private String resolveExportFileExtension(ExportTaskEntity exportTask) {
        String format = normalizeUpperRequired(exportTask.getExportFormat(), "format is required");
        return switch (format) {
            case "EXCEL" -> "xlsx";
            case "PDF" -> "pdf";
            default -> "bin";
        };
    }

    private byte[] buildImportTemplateContent(String module) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("template");
            switch (module) {
                case "TASK" -> {
                    writeSheetRow(sheet, 0, List.of("name", "description", "status", "taskType", "priority", "progress", "plannedStartDate", "plannedEndDate", "deadlineDate", "constraintType", "constraintDate", "plannedHours", "parentTaskId", "remark"));
                    writeSheetRow(sheet, 1, List.of("Requirement analysis", "Clarify scope and workflow", "TODO", "TASK", "MEDIUM", "0", "2026-04-26 09:00:00", "2026-04-28 18:00:00", "2026-04-29 18:00:00", "FNLT", "2026-04-28 18:00:00", "24", "", "Imported template sample"));
                }
                case "RISK" -> {
                    writeSheetRow(sheet, 0, List.of("name", "description", "probability", "impact", "level", "status", "responseStrategy", "taskId", "phaseName"));
                    writeSheetRow(sheet, 1, List.of("Key member turnover", "A critical developer may leave unexpectedly", "4", "5", "CRITICAL", "IDENTIFIED", "Prepare backup ownership and knowledge transfer", "", "Backend development"));
                }
                case "COST" -> {
                    writeSheetRow(sheet, 0, List.of("recordType", "type", "name", "taskId", "phase", "amount", "currency", "sourceType", "spendDate", "remark"));
                    writeSheetRow(sheet, 1, List.of("PLAN", "LABOR", "Backend labor budget", "", "Development", "12000", "CNY", "", "", "Planned cost sample"));
                    writeSheetRow(sheet, 2, List.of("ACTUAL", "", "", "", "", "3200", "CNY", "MANUAL", "2026-04-29", "Actual cost sample"));
                }
                case "TIMESHEET" -> {
                    writeSheetRow(sheet, 0, List.of("taskId", "userId", "workDate", "hours", "description"));
                    writeSheetRow(sheet, 1, List.of("10001", "", "2026-04-29", "8", "Implemented and verified assigned work"));
                }
                default -> throw new IllegalArgumentException("template module is not supported yet");
            }

            Row headerRow = sheet.getRow(0);
            autoSizeColumns(sheet, headerRow == null ? 0 : Math.max(headerRow.getLastCellNum(), 0));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void buildTaskExportSheet(Sheet sheet, Long projectId) {
        writeSheetRow(sheet, 0, List.of("ID", "Name", "Description", "Status", "TaskType", "Priority", "Progress", "PlannedStartDate", "PlannedEndDate", "DeadlineDate", "ConstraintType", "ConstraintDate", "PlannedHours", "Remark"));
        int rowIndex = 1;
        for (var item : taskMapper.selectByProjectId(projectId, null)) {
            writeSheetRow(sheet, rowIndex++, List.of(
                    safeCell(item.getId()),
                    safeCell(item.getName()),
                    safeCell(item.getDescription()),
                    safeCell(item.getStatus()),
                    safeCell(item.getTaskType()),
                    safeCell(item.getPriority()),
                    safeCell(item.getProgress()),
                    safeCell(item.getPlannedStartDate()),
                    safeCell(item.getPlannedEndDate()),
                    safeCell(item.getDeadlineDate()),
                    safeCell(item.getConstraintType()),
                    safeCell(item.getConstraintDate()),
                    safeCell(item.getPlannedHours()),
                    safeCell(item.getRemark())
            ));
        }
    }

    private void buildRiskExportSheet(Sheet sheet, Long projectId) {
        writeSheetRow(sheet, 0, List.of("ID", "RiskCode", "Name", "Description", "Probability", "Impact", "Level", "Status", "ResponseStrategy", "TaskId", "PhaseName"));
        int rowIndex = 1;
        for (var item : riskMapper.selectByProjectId(projectId, new RiskQueryDto())) {
            writeSheetRow(sheet, rowIndex++, List.of(
                    safeCell(item.getId()),
                    safeCell(item.getRiskCode()),
                    safeCell(item.getName()),
                    safeCell(item.getDescription()),
                    safeCell(item.getProbability()),
                    safeCell(item.getImpact()),
                    safeCell(item.getLevel()),
                    safeCell(item.getStatus()),
                    safeCell(item.getResponseStrategy()),
                    safeCell(item.getTaskId()),
                    safeCell(item.getPhaseName())
            ));
        }
    }

    private void buildCostExportSheet(Sheet sheet, Long projectId) {
        writeSheetRow(sheet, 0, List.of("RecordType", "ID", "Type", "Name", "TaskId", "TaskName", "Phase", "Amount", "Currency", "SourceType", "SpendDate", "Remark"));
        int rowIndex = 1;
        for (CostPlanVO item : costMapper.selectPlansByProjectId(projectId)) {
            writeSheetRow(sheet, rowIndex++, List.of(
                    "PLAN",
                    safeCell(item.getId()),
                    safeCell(item.getType()),
                    safeCell(item.getName()),
                    safeCell(item.getTaskId()),
                    safeCell(item.getTaskName()),
                    safeCell(item.getPhase()),
                    safeCell(item.getPlannedAmount()),
                    safeCell(item.getCurrency()),
                    "",
                    "",
                    safeCell(item.getRemark())
            ));
        }
        for (CostActualVO item : costMapper.selectActualsByProjectId(projectId)) {
            writeSheetRow(sheet, rowIndex++, List.of(
                    "ACTUAL",
                    safeCell(item.getId()),
                    "",
                    "",
                    safeCell(item.getTaskId()),
                    safeCell(item.getTaskName()),
                    "",
                    safeCell(item.getAmount()),
                    safeCell(item.getCurrency()),
                    safeCell(item.getSourceType()),
                    safeCell(item.getSpendDate()),
                    safeCell(item.getRemark())
            ));
        }
    }

    private void buildTimesheetExportSheet(Sheet sheet, Long projectId) {
        writeSheetRow(sheet, 0, List.of("ID", "TaskId", "TaskName", "UserId", "UserName", "WorkDate", "Hours", "CostRate", "LaborCost", "Status", "Description"));
        int rowIndex = 1;
        for (TimesheetVO item : timesheetMapper.selectByProjectId(projectId, null)) {
            writeSheetRow(sheet, rowIndex++, List.of(
                    safeCell(item.getId()),
                    safeCell(item.getTaskId()),
                    safeCell(item.getTaskName()),
                    safeCell(item.getUserId()),
                    safeCell(item.getUserName()),
                    safeCell(item.getWorkDate()),
                    safeCell(item.getHours()),
                    safeCell(item.getCostRate()),
                    safeCell(item.getLaborCost()),
                    safeCell(item.getStatus()),
                    safeCell(item.getDescription())
            ));
        }
    }

    private void buildReportExportSheet(Sheet sheet, Long projectId) {
        writeSheetRow(sheet, 0, List.of("ID", "ReportType", "Title", "Status", "StartDate", "EndDate", "GeneratedBy", "GeneratedAt", "AttachmentId", "AttachmentUrl", "ContentJson"));
        int rowIndex = 1;
        for (ReportVO item : projectClosureMapper.selectReportsByProjectId(projectId)) {
            writeSheetRow(sheet, rowIndex++, List.of(
                    safeCell(item.getId()),
                    safeCell(item.getReportType()),
                    safeCell(item.getTitle()),
                    safeCell(item.getStatus()),
                    safeCell(item.getStartDate()),
                    safeCell(item.getEndDate()),
                    safeCell(item.getGeneratedByName()),
                    safeCell(item.getGeneratedAt()),
                    safeCell(item.getAttachmentId()),
                    safeCell(item.getAttachmentUrl()),
                    safeCell(item.getContentJson())
            ));
        }
    }

    private void writeSheetRow(Sheet sheet, int rowIndex, List<String> values) {
        Row row = sheet.createRow(rowIndex);
        for (int index = 0; index < values.size(); index++) {
            row.createCell(index).setCellValue(values.get(index));
        }
    }

    private void autoSizeColumns(Sheet sheet, int count) {
        for (int index = 0; index < count; index++) {
            sheet.autoSizeColumn(index);
        }
    }

    private List<List<String>> extractSheetRows(Sheet sheet) {
        DataFormatter formatter = new DataFormatter();
        List<List<String>> rows = new ArrayList<>();
        if (sheet == null) {
            return rows;
        }
        for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null || row.getLastCellNum() < 0) {
                continue;
            }
            List<String> values = new ArrayList<>();
            boolean hasContent = false;
            for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
                Cell cell = row.getCell(cellIndex);
                String value = cell == null ? "" : formatter.formatCellValue(cell).trim();
                if (!value.isEmpty()) {
                    hasContent = true;
                }
                values.add(value);
            }
            if (hasContent) {
                rows.add(values);
            }
        }
        return rows;
    }

    private String safeCell(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String resolveExportModuleLabel(String module) {
        return switch (defaultText(module, "").trim().toUpperCase(Locale.ROOT)) {
            case "TASK" -> "Tasks";
            case "RISK" -> "Risks";
            case "COST" -> "Costs";
            case "TIMESHEET" -> "Timesheets";
            case "REPORT" -> "Reports";
            default -> defaultText(module, "Export");
        };
    }

    private PDRectangle resolvePdfPageSize(int columnCount) {
        if (columnCount > 6) {
            return new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
        }
        return PDRectangle.A4;
    }

    private PDFont loadPdfFont(PDDocument document) throws IOException {
        Path fontPath = resolvePdfFontPath();
        if (fontPath != null) {
            return PDType0Font.load(document, fontPath.toFile());
        }
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }

    private Path resolvePdfFontPath() {
        List<String> candidatePaths = List.of(
                "C:\\Windows\\Fonts\\simfang.ttf",
                "C:\\Windows\\Fonts\\simhei.ttf",
                "C:\\Windows\\Fonts\\simkai.ttf",
                "C:\\Windows\\Fonts\\msyh.ttf",
                "C:\\Windows\\Fonts\\arialuni.ttf",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.otf",
                "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.otf"
        );
        for (String candidate : candidatePaths) {
            Path path = Path.of(candidate);
            if (Files.exists(path)) {
                return path;
            }
        }

        List<Path> fontDirs = List.of(
                Path.of("C:\\Windows\\Fonts"),
                Path.of("/usr/share/fonts"),
                Path.of("/System/Library/Fonts")
        );
        List<String> keywords = List.of("noto", "cjk", "yahei", "hei", "fang", "kai", "song", "sans");
        for (Path fontDir : fontDirs) {
            if (!Files.isDirectory(fontDir)) {
                continue;
            }
            try (var files = Files.list(fontDir)) {
                for (Path path : files.sorted().toList()) {
                    String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
                    boolean isFontFile = fileName.endsWith(".ttf") || fileName.endsWith(".otf");
                    boolean matches = keywords.stream().anyMatch(fileName::contains);
                    if (isFontFile && matches) {
                        return path;
                    }
                }
            } catch (IOException ignored) {
                // Ignore and continue scanning fallback directories.
            }
        }
        return null;
    }

    private void writePdfParagraph(PdfCursor cursor, PDFont font, float fontSize, String text) throws IOException {
        List<String> lines = wrapPdfText(text, font, fontSize, cursor.maxWidth());
        float requiredHeight = Math.max(lines.size(), 1) * PDF_LINE_HEIGHT;
        cursor.ensureSpace(requiredHeight);
        for (String line : lines) {
            cursor.stream.beginText();
            cursor.stream.setFont(font, fontSize);
            cursor.stream.newLineAtOffset(PDF_MARGIN, cursor.y);
            cursor.stream.showText(line);
            cursor.stream.endText();
            cursor.y -= PDF_LINE_HEIGHT;
        }
    }

    private void writePdfTable(PdfCursor cursor, PDFont font, List<String> headers, List<List<String>> dataRows) throws IOException {
        List<Float> columnWidths = resolvePdfColumnWidths(headers, dataRows, cursor.maxWidth());
        writePdfTableRow(cursor, font, PDF_TABLE_FONT_SIZE, headers, columnWidths, true);
        for (List<String> row : dataRows) {
            float rowHeight = measurePdfTableRowHeight(row, columnWidths, font, PDF_TABLE_FONT_SIZE, false);
            if (cursor.y - rowHeight < PDF_MARGIN) {
                cursor.addPage();
                writePdfTableRow(cursor, font, PDF_TABLE_FONT_SIZE, headers, columnWidths, true);
            }
            writePdfTableRow(cursor, font, PDF_TABLE_FONT_SIZE, row, columnWidths, false);
        }
    }

    private List<Float> resolvePdfColumnWidths(List<String> headers, List<List<String>> dataRows, float tableWidth) {
        int columnCount = headers.size();
        List<Float> widths = new ArrayList<>();
        if (columnCount == 0) {
            return widths;
        }

        int sampleRows = Math.min(dataRows.size(), 40);
        float totalWeight = 0f;
        for (int index = 0; index < columnCount; index++) {
            int weight = estimatePdfColumnWeight(index < headers.size() ? headers.get(index) : "");
            for (int rowIndex = 0; rowIndex < sampleRows; rowIndex++) {
                List<String> row = dataRows.get(rowIndex);
                String value = index < row.size() ? row.get(index) : "";
                weight = Math.max(weight, estimatePdfColumnWeight(value));
            }
            float columnWidth = Math.max(PDF_TABLE_MIN_COLUMN_WIDTH, weight);
            widths.add(columnWidth);
            totalWeight += columnWidth;
        }

        if (totalWeight <= 0f) {
            float equalWidth = tableWidth / columnCount;
            for (int index = 0; index < columnCount; index++) {
                widths.set(index, equalWidth);
            }
            return widths;
        }

        float scale = tableWidth / totalWeight;
        float sum = 0f;
        for (int index = 0; index < widths.size(); index++) {
            float scaled = widths.get(index) * scale;
            widths.set(index, scaled);
            sum += scaled;
        }
        float diff = tableWidth - sum;
        widths.set(widths.size() - 1, widths.get(widths.size() - 1) + diff);
        return widths;
    }

    private int estimatePdfColumnWeight(String text) {
        String sample = normalizePdfCellText(text);
        if (sample.isBlank()) {
            return Math.round(PDF_TABLE_MIN_COLUMN_WIDTH);
        }
        int visibleWeight = 0;
        int limit = Math.min(sample.length(), 32);
        for (int index = 0; index < limit; index++) {
            char ch = sample.charAt(index);
            visibleWeight += ch <= 127 ? 6 : 10;
        }
        return Math.max(Math.round(PDF_TABLE_MIN_COLUMN_WIDTH), Math.min(visibleWeight + 16, 140));
    }

    private float writePdfTableRow(PdfCursor cursor,
                                   PDFont font,
                                   float fontSize,
                                   List<String> values,
                                   List<Float> columnWidths,
                                   boolean headerRow) throws IOException {
        float rowHeight = measurePdfTableRowHeight(values, columnWidths, font, fontSize, headerRow);
        cursor.ensureSpace(rowHeight);

        float x = PDF_MARGIN;
        float topY = cursor.y;
        float bottomY = topY - rowHeight;

        if (headerRow) {
            cursor.stream.setNonStrokingColor(245, 247, 250);
            for (float columnWidth : columnWidths) {
                cursor.stream.addRect(x, bottomY, columnWidth, rowHeight);
                cursor.stream.fill();
                x += columnWidth;
            }
            cursor.stream.setNonStrokingColor(0, 0, 0);
            x = PDF_MARGIN;
        }

        cursor.stream.setLineWidth(0.6f);
        cursor.stream.setStrokingColor(205, 210, 218);
        for (float columnWidth : columnWidths) {
            cursor.stream.addRect(x, bottomY, columnWidth, rowHeight);
            cursor.stream.stroke();
            x += columnWidth;
        }
        cursor.stream.setStrokingColor(0, 0, 0);

        x = PDF_MARGIN;
        for (int index = 0; index < columnWidths.size(); index++) {
            float columnWidth = columnWidths.get(index);
            String value = index < values.size() ? values.get(index) : "";
            List<String> lines = buildPdfCellLines(value, font, fontSize, columnWidth - PDF_TABLE_CELL_PADDING * 2, headerRow);
            float textY = topY - PDF_TABLE_CELL_PADDING - fontSize;
            for (String line : lines) {
                cursor.stream.beginText();
                cursor.stream.setFont(font, fontSize);
                cursor.stream.newLineAtOffset(x + PDF_TABLE_CELL_PADDING, textY);
                cursor.stream.showText(line);
                cursor.stream.endText();
                textY -= PDF_TABLE_LINE_HEIGHT;
            }
            x += columnWidth;
        }

        cursor.y = bottomY;
        return rowHeight;
    }

    private float measurePdfTableRowHeight(List<String> values,
                                           List<Float> columnWidths,
                                           PDFont font,
                                           float fontSize,
                                           boolean headerRow) throws IOException {
        int maxLines = 1;
        for (int index = 0; index < columnWidths.size(); index++) {
            String value = index < values.size() ? values.get(index) : "";
            List<String> lines = buildPdfCellLines(value, font, fontSize, columnWidths.get(index) - PDF_TABLE_CELL_PADDING * 2, headerRow);
            maxLines = Math.max(maxLines, lines.size());
        }
        return maxLines * PDF_TABLE_LINE_HEIGHT + PDF_TABLE_CELL_PADDING * 2;
    }

    private List<String> buildPdfCellLines(String text,
                                           PDFont font,
                                           float fontSize,
                                           float maxWidth,
                                           boolean headerRow) throws IOException {
        float safeWidth = Math.max(maxWidth, 8f);
        int maxLines = headerRow ? PDF_TABLE_HEADER_MAX_LINES : PDF_TABLE_BODY_MAX_LINES;
        List<String> wrapped = wrapPdfText(normalizePdfCellText(text), font, fontSize, safeWidth);
        if (wrapped.size() <= maxLines) {
            return wrapped;
        }

        List<String> limited = new ArrayList<>(wrapped.subList(0, maxLines));
        limited.set(maxLines - 1, ellipsizePdfText(limited.get(maxLines - 1), font, fontSize, safeWidth));
        return limited;
    }

    private String normalizePdfCellText(String text) {
        return defaultText(text, "")
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replace('\t', ' ')
                .trim()
                .replaceAll("\\s{2,}", " ");
    }

    private String ellipsizePdfText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        String value = defaultText(text, "");
        if (value.isBlank()) {
            return value;
        }
        String ellipsis = "...";
        float ellipsisWidth = font.getStringWidth(ellipsis) / 1000 * fontSize;
        if (ellipsisWidth >= maxWidth) {
            return value;
        }

        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            String candidate = builder + String.valueOf(value.charAt(index));
            float width = font.getStringWidth(candidate) / 1000 * fontSize;
            if (width + ellipsisWidth > maxWidth) {
                break;
            }
            builder.append(value.charAt(index));
        }
        if (builder.length() == value.length()) {
            return value;
        }
        return builder.append(ellipsis).toString();
    }

    private List<String> wrapPdfText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        String safeText = defaultText(text, "");
        if (safeText.isBlank()) {
            return List.of("");
        }
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int index = 0; index < safeText.length(); index++) {
            char ch = safeText.charAt(index);
            if (ch == '\n' || ch == '\r') {
                if (current.length() > 0) {
                    lines.add(current.toString());
                    current = new StringBuilder();
                }
                continue;
            }
            String candidate = current + String.valueOf(ch);
            float width = font.getStringWidth(candidate) / 1000 * fontSize;
            if (width > maxWidth && current.length() > 0) {
                lines.add(current.toString());
                current = new StringBuilder();
            }
            current.append(ch);
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines.isEmpty() ? List.of("") : lines;
    }

    private String buildTemplateFileName(String module) {
        return switch (module) {
            case "TASK" -> "task_import_template.xlsx";
            case "RISK" -> "risk_import_template.xlsx";
            case "COST" -> "cost_import_template.xlsx";
            case "TIMESHEET" -> "timesheet_import_template.xlsx";
            default -> throw new IllegalArgumentException("template module is not supported yet");
        };
    }

    private List<Map<String, String>> readImportRows(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".csv")) {
            return readCsvRows(file.getInputStream());
        }
        return readExcelRows(file.getInputStream());
    }

    private List<Map<String, String>> readCsvRows(InputStream inputStream) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return rows;
            }
            List<String> headers = parseCsvLine(headerLine);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> values = parseCsvLine(line);
                rows.add(buildRowMap(headers, values));
            }
        }
        return rows;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString().trim());
        return values;
    }

    private List<Map<String, String>> readExcelRows(InputStream inputStream) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                return rows;
            }
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                return rows;
            }

            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                headers.add(normalizeHeader(formatter.formatCellValue(headerRow.getCell(i))));
            }

            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                List<String> values = new ArrayList<>();
                boolean hasValue = false;
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    String value = formatter.formatCellValue(cell).trim();
                    if (!value.isEmpty()) {
                        hasValue = true;
                    }
                    values.add(value);
                }
                if (!hasValue) {
                    continue;
                }
                rows.add(buildRowMap(headers, values));
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
        return rows;
    }

    private Map<String, String> buildRowMap(List<String> headers, List<String> values) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            map.put(headers.get(i), i < values.size() ? values.get(i) : "");
        }
        return map;
    }

    private ImportResultVO importTasks(Long projectId, List<Map<String, String>> rows) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int baseSortOrder = Objects.requireNonNullElse(
                taskMapper.selectByProjectId(projectId, null).stream()
                        .map(item -> item.getSortOrder() == null ? 0 : item.getSortOrder())
                        .max(Integer::compareTo)
                        .orElse(0),
                0
        );

        for (int index = 0; index < rows.size(); index++) {
            Map<String, String> row = rows.get(index);
            try {
                String name = requireText(row, "name", "title");
                TaskEntity entity = new TaskEntity();
                LocalDateTime now = LocalDateTime.now();
                entity.setId(IdWorker.getId());
                entity.setProjectId(projectId);
                entity.setParentTaskId(parseNullableLong(findValue(row, "parentTaskId", "parenttaskid")));
                if (entity.getParentTaskId() != null && taskMapper.selectEntityById(projectId, entity.getParentTaskId()) == null) {
                    throw new IllegalArgumentException("parent task not found");
                }
                entity.setTaskCode("TASK" + entity.getId());
                entity.setName(name);
                entity.setDescription(nullableText(findValue(row, "description")));
                entity.setTaskType(parseTaskType(findValue(row, "taskType", "tasktype", "type")));
                entity.setPriority(parseTaskPriority(findValue(row, "priority")));
                entity.setStatus(parseTaskStatus(findValue(row, "status")));
                entity.setProgress(parseNullableDecimal(findValue(row, "progress"), BigDecimal.ZERO));
                entity.setPlannedStartDate(parseNullableDateTime(findValue(row, "plannedStartDate", "plannedstartdate", "start")));
                entity.setPlannedEndDate(parseNullableDateTime(findValue(row, "plannedEndDate", "plannedenddate", "finish", "end")));
                entity.setDeadlineDate(parseNullableDateTime(findValue(row, "deadlineDate", "deadlinedate", "deadline")));
                entity.setConstraintType(parseTaskConstraintType(findValue(row, "constraintType", "constrainttype")));
                entity.setConstraintDate(parseNullableDateTime(findValue(row, "constraintDate", "constraintdate")));
                entity.setPlannedHours(parseNullableDecimal(findValue(row, "plannedHours", "plannedhours", "duration"), BigDecimal.ZERO));
                entity.setSortOrder(baseSortOrder + index + 1);
                entity.setRemark(nullableText(findValue(row, "remark", "mode")));
                entity.setCreatedBy(UserContextHolder.getUserId());
                entity.setCreatedAt(now);
                entity.setUpdatedBy(UserContextHolder.getUserId());
                entity.setUpdatedAt(now);
                entity.setDeleted(0);
                taskMapper.insert(entity);
                successCount++;
            } catch (Exception ex) {
                errors.add(String.format("Row %d task import failed: %s", index + 2, ex.getMessage()));
            }
        }
        if (successCount > 0) {
            taskProgressRollupService.syncAll(projectId);
        }

        return buildImportResult(successCount, errors);
    }

    private ImportResultVO importRisks(Long projectId, List<Map<String, String>> rows) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        for (int index = 0; index < rows.size(); index++) {
            Map<String, String> row = rows.get(index);
            try {
                String name = requireText(row, "name", "title");
                Integer probability = parseRequiredInteger(row, "probability");
                Integer impact = parseRequiredInteger(row, "impact");
                Long taskId = parseNullableLong(findValue(row, "taskId", "taskid"));
                validateTask(projectId, taskId);

                RiskEntity entity = new RiskEntity();
                LocalDateTime now = LocalDateTime.now();
                entity.setId(IdWorker.getId());
                entity.setProjectId(projectId);
                entity.setRiskCode("RISK" + entity.getId());
                entity.setName(name);
                entity.setDescription(nullableText(findValue(row, "description")));
                entity.setProbability(probability);
                entity.setImpact(impact);
                entity.setLevel(parseRiskLevel(findValue(row, "level"), probability, impact));
                entity.setStatus(parseRiskStatus(findValue(row, "status")));
                entity.setResponseStrategy(nullableText(findValue(row, "responseStrategy", "responsestrategy")));
                entity.setTaskId(taskId);
                entity.setPhaseName(nullableText(findValue(row, "phaseName", "phasename")));
                entity.setOwnerId(parseNullableLong(findValue(row, "ownerId", "ownerid")));
                validateOwner(entity.getOwnerId());
                entity.setIdentifiedAt(now);
                entity.setCreatedBy(UserContextHolder.getUserId());
                entity.setCreatedAt(now);
                entity.setUpdatedBy(UserContextHolder.getUserId());
                entity.setUpdatedAt(now);
                entity.setDeleted(0);
                riskMapper.insert(entity);
                successCount++;
            } catch (Exception ex) {
                errors.add(String.format("Row %d risk import failed: %s", index + 2, ex.getMessage()));
            }
        }

        return buildImportResult(successCount, errors);
    }

    private ImportResultVO importCosts(Long projectId, List<Map<String, String>> rows) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        for (int index = 0; index < rows.size(); index++) {
            Map<String, String> row = rows.get(index);
            try {
                String recordType = parseCostRecordType(findValue(row, "recordType", "recordtype"), row);
                if ("PLAN".equals(recordType)) {
                    CostPlanEntity entity = new CostPlanEntity();
                    LocalDateTime now = LocalDateTime.now();
                    entity.setId(IdWorker.getId());
                    entity.setProjectId(projectId);
                    entity.setType(parseCostPlanType(findValue(row, "type")));
                    entity.setName(requireText(row, "name"));
                    entity.setTaskId(parseNullableLong(findValue(row, "taskId", "taskid")));
                    validateTask(projectId, entity.getTaskId());
                    entity.setPhase(nullableText(findValue(row, "phase")));
                    entity.setPlannedAmount(parseRequiredDecimal(row, "amount", "plannedAmount", "plannedamount"));
                    entity.setCurrency(defaultText(nullableText(findValue(row, "currency")), "CNY").toUpperCase(Locale.ROOT));
                    entity.setRemark(nullableText(findValue(row, "remark")));
                    entity.setCreatedBy(UserContextHolder.getUserId());
                    entity.setCreatedAt(now);
                    entity.setUpdatedBy(UserContextHolder.getUserId());
                    entity.setUpdatedAt(now);
                    entity.setDeleted(0);
                    costMapper.insertPlan(entity);
                } else {
                    CostActualEntity entity = new CostActualEntity();
                    LocalDateTime now = LocalDateTime.now();
                    entity.setId(IdWorker.getId());
                    entity.setProjectId(projectId);
                    entity.setTaskId(parseNullableLong(findValue(row, "taskId", "taskid")));
                    validateTask(projectId, entity.getTaskId());
                    entity.setSourceType(parseCostSourceType(findValue(row, "sourceType", "sourcetype")));
                    entity.setActualAmount(parseRequiredDecimal(row, "amount"));
                    entity.setOccurredDate(parseRequiredLocalDate(row, "spendDate", "spenddate", "occurredDate", "occurreddate"));
                    entity.setDescription(nullableText(findValue(row, "remark", "description")));
                    entity.setRecorderId(UserContextHolder.getUserId());
                    entity.setCreatedAt(now);
                    entity.setUpdatedAt(now);
                    entity.setDeleted(0);
                    costMapper.insertActual(entity);
                }
                successCount++;
            } catch (Exception ex) {
                errors.add(String.format("Row %d cost import failed: %s", index + 2, ex.getMessage()));
            }
        }

        return buildImportResult(successCount, errors);
    }

    private ImportResultVO importTimesheets(Long projectId, List<Map<String, String>> rows) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        Set<Long> touchedTaskIds = new HashSet<>();

        for (int index = 0; index < rows.size(); index++) {
            Map<String, String> row = rows.get(index);
            try {
                Long taskId = parseRequiredLong(row, "taskId", "taskid");
                TaskEntity task = requireTask(projectId, taskId);
                Long userId = parseNullableLong(findValue(row, "userId", "userid"));
                if (userId == null) {
                    userId = UserContextHolder.getUserId();
                }
                validateUser(userId);

                TimesheetEntity entity = new TimesheetEntity();
                LocalDateTime now = LocalDateTime.now();
                entity.setId(IdWorker.getId());
                entity.setProjectId(projectId);
                entity.setTaskId(taskId);
                entity.setUserId(userId);
                entity.setWorkDate(parseRequiredLocalDate(row, "workDate", "workdate"));
                entity.setHours(parseRequiredDecimal(row, "hours"));
                entity.setCostRate(resolveTimesheetCostRate(projectId, task));
                entity.setLaborCost(resolveTimesheetLaborCost(entity.getHours(), entity.getCostRate()));
                entity.setDescription(nullableText(findValue(row, "description", "remark")));
                entity.setStatus("SUBMITTED");
                entity.setCreatedAt(now);
                entity.setUpdatedAt(now);
                entity.setDeleted(0);
                timesheetMapper.insert(entity);
                touchedTaskIds.add(taskId);
                successCount++;
            } catch (Exception ex) {
                errors.add(String.format("Row %d timesheet import failed: %s", index + 2, ex.getMessage()));
            }
        }

        for (Long taskId : touchedTaskIds) {
            syncTaskActualMetrics(projectId, taskId);
        }
        return buildImportResult(successCount, errors);
    }

    private ImportResultVO buildImportResult(int successCount, List<String> errors) {
        ImportResultVO vo = new ImportResultVO();
        vo.setSuccessCount(successCount);
        vo.setFailCount(errors.size());
        vo.setErrors(errors);
        return vo;
    }

    private String requireText(Map<String, String> row, String... keys) {
        String value = nullableText(findValue(row, keys));
        if (value == null) {
            throw new IllegalArgumentException("required field is missing");
        }
        return value;
    }

    private Integer parseRequiredInteger(Map<String, String> row, String... keys) {
        String value = findValue(row, keys);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("required number is missing");
        }
        return Integer.parseInt(value.trim());
    }

    private Long parseRequiredLong(Map<String, String> row, String... keys) {
        String value = findValue(row, keys);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("required id is missing");
        }
        return Long.parseLong(value.trim());
    }

    private BigDecimal parseRequiredDecimal(Map<String, String> row, String... keys) {
        String value = findValue(row, keys);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("required amount is missing");
        }
        return new BigDecimal(value.trim());
    }

    private String findValue(Map<String, String> row, String... keys) {
        for (String key : keys) {
            String normalized = normalizeHeader(key);
            if (row.containsKey(normalized) && row.get(normalized) != null && !row.get(normalized).isBlank()) {
                return row.get(normalized).trim();
            }
        }
        return null;
    }

    private String normalizeHeader(String header) {
        return header == null ? "" : header.replace(" ", "").replace("_", "").toLowerCase(Locale.ROOT);
    }

    private String nullableText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String defaultText(String value, String defaultValue) {
        String normalized = nullableText(value);
        return normalized == null ? defaultValue : normalized;
    }

    private String normalizeAuditModule(String value) {
        String normalized = nullableText(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeUpperRequired(String value, String message) {
        String normalized = nullableText(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private Long parseNullableLong(String value) {
        String normalized = nullableText(value);
        return normalized == null ? null : Long.parseLong(normalized);
    }

    private BigDecimal parseNullableDecimal(String value, BigDecimal defaultValue) {
        String normalized = nullableText(value);
        return normalized == null ? defaultValue : new BigDecimal(normalized);
    }

    private LocalDateTime parseNullableDateTime(String value) {
        String normalized = nullableText(value);
        if (normalized == null) {
            return null;
        }
        for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(normalized, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(normalized, formatter).atTime(LocalTime.MIN);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new IllegalArgumentException("invalid datetime: " + normalized);
    }

    private LocalDate parseRequiredLocalDate(Map<String, String> row, String... keys) {
        String value = findValue(row, keys);
        LocalDate result = parseNullableLocalDate(value);
        if (result == null) {
            throw new IllegalArgumentException("required date is missing");
        }
        return result;
    }

    private LocalDate parseNullableLocalDate(String value) {
        String normalized = nullableText(value);
        if (normalized == null) {
            return null;
        }
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(normalized, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        try {
            return parseNullableDateTime(normalized).toLocalDate();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid date: " + normalized);
        }
    }

    private String parseTaskType(String value) {
        String normalized = nullableText(value);
        if (normalized == null) {
            return TaskTypeEnum.TASK.name();
        }
        return switch (normalized.toUpperCase(Locale.ROOT)) {
            case "TASK" -> TaskTypeEnum.TASK.name();
            case "SUB_TASK", "SUBTASK" -> TaskTypeEnum.SUB_TASK.name();
            case "MILESTONE_TASK", "MILESTONE" -> TaskTypeEnum.MILESTONE_TASK.name();
            default -> throw new IllegalArgumentException("invalid task type: " + normalized);
        };
    }

    private String parseTaskPriority(String value) {
        String normalized = nullableText(value);
        if (normalized == null) {
            return "MEDIUM";
        }
        return switch (normalized.toUpperCase(Locale.ROOT)) {
            case "LOW", "MEDIUM", "HIGH", "URGENT" -> normalized.toUpperCase(Locale.ROOT);
            default -> throw new IllegalArgumentException("invalid task priority: " + normalized);
        };
    }

    private String parseTaskStatus(String value) {
        String normalized = nullableText(value);
        if (normalized == null) {
            return TaskStatusEnum.TODO.name();
        }
        return switch (normalized.toUpperCase(Locale.ROOT)) {
            case "TODO", "NOT_STARTED" -> TaskStatusEnum.TODO.name();
            case "IN_PROGRESS", "STARTED" -> TaskStatusEnum.IN_PROGRESS.name();
            case "PENDING_REVIEW", "REVIEWING", "PENDING_ACCEPTANCE" -> TaskStatusEnum.PENDING_REVIEW.name();
            case "BLOCKED" -> TaskStatusEnum.BLOCKED.name();
            case "DONE", "COMPLETED", "FINISHED" -> TaskStatusEnum.DONE.name();
            default -> throw new IllegalArgumentException("invalid task status: " + normalized);
        };
    }

    private String parseTaskConstraintType(String value) {
        String normalized = nullableText(value);
        if (normalized == null) {
            return null;
        }
        try {
            return TaskConstraintTypeEnum.valueOf(normalized.toUpperCase(Locale.ROOT)).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid task constraint type: " + normalized);
        }
    }

    private String parseRiskStatus(String value) {
        String normalized = nullableText(value);
        if (normalized == null) {
            return RiskStatusEnum.IDENTIFIED.name();
        }
        return switch (normalized.toUpperCase(Locale.ROOT)) {
            case "IDENTIFIED" -> RiskStatusEnum.IDENTIFIED.name();
            case "ANALYZED" -> RiskStatusEnum.ANALYZED.name();
            case "RESPONDING" -> RiskStatusEnum.RESPONDING.name();
            case "CLOSED" -> RiskStatusEnum.CLOSED.name();
            default -> throw new IllegalArgumentException("invalid risk status: " + normalized);
        };
    }

    private String parseRiskLevel(String value, Integer probability, Integer impact) {
        String normalized = nullableText(value);
        if (normalized != null) {
            String upper = normalized.toUpperCase(Locale.ROOT);
            if (List.of("LOW", "MEDIUM", "HIGH", "CRITICAL").contains(upper)) {
                return upper;
            }
            throw new IllegalArgumentException("invalid risk level: " + normalized);
        }
        int score = probability * impact;
        if (score >= 16) {
            return "CRITICAL";
        }
        if (score >= 10) {
            return "HIGH";
        }
        if (score >= 5) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String parseCostRecordType(String value, Map<String, String> row) {
        String normalized = nullableText(value);
        if (normalized == null) {
            if (nullableText(findValue(row, "sourceType", "sourcetype")) != null
                    || nullableText(findValue(row, "spendDate", "spenddate", "occurredDate", "occurreddate")) != null) {
                return "ACTUAL";
            }
            return "PLAN";
        }
        return switch (normalized.toUpperCase(Locale.ROOT)) {
            case "PLAN" -> "PLAN";
            case "ACTUAL" -> "ACTUAL";
            default -> throw new IllegalArgumentException("invalid cost record type: " + normalized);
        };
    }

    private String parseCostPlanType(String value) {
        String normalized = nullableText(value);
        if (normalized == null) {
            return "LABOR";
        }
        return switch (normalized.toUpperCase(Locale.ROOT)) {
            case "LABOR", "PROCUREMENT", "EQUIPMENT", "OTHER" -> normalized.toUpperCase(Locale.ROOT);
            default -> throw new IllegalArgumentException("invalid cost plan type: " + normalized);
        };
    }

    private String parseCostSourceType(String value) {
        String normalized = nullableText(value);
        if (normalized == null) {
            return "MANUAL";
        }
        return switch (normalized.toUpperCase(Locale.ROOT)) {
            case "MANUAL", "EXPENSE", "PURCHASE", "TIMESHEET" -> normalized.toUpperCase(Locale.ROOT);
            default -> throw new IllegalArgumentException("invalid cost source type: " + normalized);
        };
    }

    private BigDecimal resolveTimesheetCostRate(Long projectId, TaskEntity task) {
        BigDecimal plannedHours = task.getPlannedHours();
        if (plannedHours == null || plannedHours.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal laborPlanAmount = costMapper.sumPlannedLaborAmountByTask(projectId, task.getId());
        if (laborPlanAmount != null && laborPlanAmount.compareTo(BigDecimal.ZERO) > 0) {
            return laborPlanAmount.divide(plannedHours, 2, RoundingMode.HALF_UP);
        }

        BigDecimal plannedCost = task.getPlannedCost();
        if (plannedCost != null && plannedCost.compareTo(BigDecimal.ZERO) > 0) {
            return plannedCost.divide(plannedHours, 2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveTimesheetLaborCost(BigDecimal hours, BigDecimal costRate) {
        BigDecimal safeHours = hours == null ? BigDecimal.ZERO : hours;
        BigDecimal safeRate = costRate == null ? BigDecimal.ZERO : costRate;
        return safeHours.multiply(safeRate).setScale(2, RoundingMode.HALF_UP);
    }

    private void syncTaskActualMetrics(Long projectId, Long taskId) {
        TaskEntity task = taskMapper.selectEntityById(projectId, taskId);
        if (task == null) {
            return;
        }

        BigDecimal actualHours = timesheetMapper.sumHoursByTaskId(projectId, taskId);
        BigDecimal actualCost = timesheetMapper.sumLaborCostByTaskId(projectId, taskId);
        taskMapper.updateActualMetrics(
                projectId,
                taskId,
                actualHours == null ? BigDecimal.ZERO : actualHours,
                actualCost == null ? BigDecimal.ZERO : actualCost,
                UserContextHolder.getUserId(),
                LocalDateTime.now()
        );
    }

    private void parseExportModule(String module) {
        ExportModuleEnum.valueOf(module);
    }

    private void parseExportFormat(String format) {
        ExportFormatEnum.valueOf(format);
    }
}
