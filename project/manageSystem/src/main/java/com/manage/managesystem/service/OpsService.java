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
import com.manage.managesystem.entity.ExportTaskEntity;
import com.manage.managesystem.entity.RiskEntity;
import com.manage.managesystem.entity.TaskEntity;
import com.manage.managesystem.enums.ExportFormatEnum;
import com.manage.managesystem.enums.ExportModuleEnum;
import com.manage.managesystem.enums.RiskStatusEnum;
import com.manage.managesystem.enums.TaskStatusEnum;
import com.manage.managesystem.enums.TaskTypeEnum;
import com.manage.managesystem.mapper.OpsMapper;
import com.manage.managesystem.mapper.ProjectMapper;
import com.manage.managesystem.mapper.RiskMapper;
import com.manage.managesystem.mapper.TaskMapper;
import com.manage.managesystem.mapper.UserMapper;
import com.manage.managesystem.vo.AcceptanceItemVO;
import com.manage.managesystem.vo.AuditLogVO;
import com.manage.managesystem.vo.ExportTaskVO;
import com.manage.managesystem.vo.ImportResultVO;
import com.manage.managesystem.vo.NotificationVO;
import com.manage.managesystem.vo.SearchResultVO;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class OpsService {
    public record DownloadTemplate(String fileName, byte[] content) {}

    private final OpsMapper opsMapper;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;
    private final TaskMapper taskMapper;
    private final RiskMapper riskMapper;
    private final ObjectMapper objectMapper;

    public OpsService(OpsMapper opsMapper, ProjectMapper projectMapper, UserMapper userMapper,
                      TaskMapper taskMapper, RiskMapper riskMapper, ObjectMapper objectMapper) {
        this.opsMapper = opsMapper;
        this.projectMapper = projectMapper;
        this.userMapper = userMapper;
        this.taskMapper = taskMapper;
        this.riskMapper = riskMapper;
        this.objectMapper = objectMapper;
    }

    public List<AcceptanceItemVO> listAcceptanceItems(Long projectId) {
        ensureProject(projectId);
        return opsMapper.selectAcceptanceItems(projectId);
    }

    @Transactional
    public AcceptanceItemVO createAcceptanceItem(Long projectId, CreateAcceptanceItemDto dto) {
        ensureProject(projectId);
        validateOwner(dto.getOwnerId());
        Long id = IdWorker.getId();
        LocalDateTime now = LocalDateTime.now();
        opsMapper.insertAcceptanceItem(id, projectId, dto.getItemName(), dto.getDescription(),
                dto.getStatus() == null || dto.getStatus().isBlank() ? "PENDING" : dto.getStatus(),
                dto.getOwnerId(), isCompleted(dto.getStatus()) ? now : null,
                buildAcceptanceRemark(dto.getItemType(), dto.getAttachmentId()), now, now);
        return requireAcceptanceItem(projectId, id);
    }

    @Transactional
    public AcceptanceItemVO updateAcceptanceItem(Long projectId, Long id, UpdateAcceptanceItemDto dto) {
        ensureProject(projectId);
        validateOwner(dto.getOwnerId());
        requireAcceptanceItem(projectId, id);
        opsMapper.updateAcceptanceItem(projectId, id, dto.getItemName(), dto.getDescription(),
                dto.getStatus() == null || dto.getStatus().isBlank() ? "PENDING" : dto.getStatus(),
                dto.getOwnerId(), isCompleted(dto.getStatus()) ? LocalDateTime.now() : null,
                buildAcceptanceRemark(dto.getItemType(), dto.getAttachmentId()), LocalDateTime.now());
        return requireAcceptanceItem(projectId, id);
    }

    @Transactional
    public void deleteAcceptanceItem(Long projectId, Long id) {
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
        if (dto.getProjectId() != null) {
            ensureProject(dto.getProjectId());
        }
        parseExportModule(dto.getModule());
        parseExportFormat(dto.getFormat());
        if (!ExportFormatEnum.EXCEL.name().equalsIgnoreCase(dto.getFormat())) {
            throw new IllegalArgumentException("only EXCEL export is supported now");
        }
        ExportTaskEntity entity = new ExportTaskEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(dto.getProjectId());
        entity.setModuleName(dto.getModule());
        entity.setExportFormat(dto.getFormat());
        entity.setFilterJson(writeJson(dto.getFilters()));
        entity.setStatus("PENDING");
        entity.setRequestedBy(UserContextHolder.getUserId());
        entity.setRequestedAt(LocalDateTime.now());
        opsMapper.insertExportTask(entity);
        AttachmentEntity attachment = generateExportAttachment(entity);
        opsMapper.insertAttachment(attachment);
        opsMapper.updateExportTaskResult(entity.getId(), "COMPLETED", attachment.getId(), LocalDateTime.now());
        return opsMapper.selectExportTaskById(entity.getId());
    }

    @Transactional
    public ImportResultVO importExcel(MultipartFile file, String module, Long projectId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("import file cannot be empty");
        }
        if (projectId != null) {
            ensureProject(projectId);
        }
        String targetModule = module == null ? "" : module.trim().toUpperCase(Locale.ROOT);
        parseExportModule(targetModule);

        try {
            List<Map<String, String>> rows = readImportRows(file);
            if ("TASK".equals(targetModule)) {
                return importTasks(projectId, rows);
            }
            if ("RISK".equals(targetModule)) {
                return importRisks(projectId, rows);
            }
            throw new IllegalArgumentException("import module is not supported yet");
        } catch (IOException e) {
            throw new IllegalStateException("import file parse failed", e);
        }
    }

    public DownloadTemplate downloadImportTemplate(String module) {
        String targetModule = module == null ? "" : module.trim().toUpperCase(Locale.ROOT);
        parseExportModule(targetModule);
        try {
            return new DownloadTemplate(buildTemplateFileName(targetModule), buildImportTemplateContent(targetModule));
        } catch (IOException e) {
            throw new IllegalStateException("template generate failed", e);
        }
    }

    public PageResult<SearchResultVO> search(SearchQueryDto query) {
        List<SearchResultVO> list = opsMapper.search(query.getKeyword(), query.getType(), query.getProjectId());
        PageResult<SearchResultVO> result = new PageResult<>();
        result.setList(list);
        result.setPage(query.getPage() == null ? 1 : query.getPage());
        result.setPageSize(query.getPageSize() == null ? list.size() : query.getPageSize());
        result.setTotal((long) list.size());
        return result;
    }

    public PageResult<AuditLogVO> auditLogs(AuditLogQueryDto query) {
        List<AuditLogVO> list = opsMapper.selectAuditLogs(query);
        PageResult<AuditLogVO> result = new PageResult<>();
        result.setList(list);
        result.setPage(query.getPage() == null ? 1 : query.getPage());
        result.setPageSize(query.getPageSize() == null ? list.size() : query.getPageSize());
        result.setTotal((long) list.size());
        return result;
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

    private void validateOwner(Long ownerId) {
        if (ownerId != null && userMapper.selectById(ownerId) == null) {
            throw new IllegalArgumentException("owner not found: " + ownerId);
        }
    }

    private boolean isCompleted(String status) {
        return "COMPLETED".equalsIgnoreCase(status);
    }

    private String buildAcceptanceRemark(String itemType, Long attachmentId) {
        return writeJson(Map.of(
                "itemType", itemType,
                "attachmentId", attachmentId
        ));
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
            attachment.setFileType("xlsx");
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
        String module = exportTask.getModuleName() == null ? "" : exportTask.getModuleName().trim().toUpperCase(Locale.ROOT);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("导出数据");
            if ("TASK".equals(module)) {
                buildTaskExportSheet(sheet, exportTask.getProjectId());
            } else if ("RISK".equals(module)) {
                buildRiskExportSheet(sheet, exportTask.getProjectId());
            } else {
                throw new IllegalArgumentException("export module is not supported yet");
            }
            autoSizeColumns(sheet, 11);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private String buildTaskExportCsv(Long projectId) {
        StringBuilder builder = new StringBuilder();
        builder.append("ID,任务名称,描述,状态,类型,优先级,进度,开始时间,完成时间,工期,备注").append('\n');
        taskMapper.selectByProjectId(projectId, null).forEach(item -> {
            builder.append(csvValue(item.getId()))
                    .append(',').append(csvValue(item.getName()))
                    .append(',').append(csvValue(item.getDescription()))
                    .append(',').append(csvValue(item.getStatus()))
                    .append(',').append(csvValue(item.getTaskType()))
                    .append(',').append(csvValue(item.getPriority()))
                    .append(',').append(csvValue(item.getProgress()))
                    .append(',').append(csvValue(item.getPlannedStartDate()))
                    .append(',').append(csvValue(item.getPlannedEndDate()))
                    .append(',').append(csvValue(item.getPlannedHours()))
                    .append(',').append(csvValue(item.getRemark()))
                    .append('\n');
        });
        return builder.toString();
    }

    private String buildRiskExportCsv(Long projectId) {
        StringBuilder builder = new StringBuilder();
        builder.append("ID,风险编号,风险名称,描述,概率,影响,等级,状态,应对策略,关联任务ID,关联阶段").append('\n');
        riskMapper.selectByProjectId(projectId, new RiskQueryDto()).forEach(item -> {
            builder.append(csvValue(item.getId()))
                    .append(',').append(csvValue(item.getRiskCode()))
                    .append(',').append(csvValue(item.getName()))
                    .append(',').append(csvValue(item.getDescription()))
                    .append(',').append(csvValue(item.getProbability()))
                    .append(',').append(csvValue(item.getImpact()))
                    .append(',').append(csvValue(item.getLevel()))
                    .append(',').append(csvValue(item.getStatus()))
                    .append(',').append(csvValue(item.getResponseStrategy()))
                    .append(',').append(csvValue(item.getTaskId()))
                    .append(',').append(csvValue(item.getPhaseName()))
                    .append('\n');
        });
        return builder.toString();
    }

    private String buildExportFileName(ExportTaskEntity exportTask) {
        String projectPart = exportTask.getProjectId() == null ? "global" : String.valueOf(exportTask.getProjectId());
        String modulePart = exportTask.getModuleName() == null ? "export" : exportTask.getModuleName().toLowerCase(Locale.ROOT);
        return String.format("%s_%s_%d.xlsx", modulePart, projectPart, exportTask.getId());
    }

    private String csvValue(Object value) {
        if (value == null) {
            return "\"\"";
        }
        String text = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + text + "\"";
    }

    private byte[] buildImportTemplateContent(String module) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("导入模板");
            if ("TASK".equals(module)) {
                writeSheetRow(sheet, 0, List.of("name", "description", "status", "taskType", "priority", "progress", "plannedStartDate", "plannedEndDate", "plannedHours", "parentTaskId", "remark"));
                writeSheetRow(sheet, 1, List.of("需求分析", "梳理业务流程和范围", "TODO", "TASK", "MEDIUM", "0", "2026-04-26 09:00:00", "2026-04-28 18:00:00", "24", "", "手动计划"));
            } else if ("RISK".equals(module)) {
                writeSheetRow(sheet, 0, List.of("name", "description", "probability", "impact", "level", "status", "responseStrategy", "taskId", "phaseName"));
                writeSheetRow(sheet, 1, List.of("核心人员流失", "关键开发离职导致延期", "4", "5", "CRITICAL", "IDENTIFIED", "提前安排备份人员与知识交接", "", "2.1 | 后端开发"));
            } else {
                throw new IllegalArgumentException("template module is not supported yet");
            }
            autoSizeColumns(sheet, 11);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void buildTaskExportSheet(Sheet sheet, Long projectId) {
        writeSheetRow(sheet, 0, List.of("ID", "任务名称", "描述", "状态", "类型", "优先级", "进度", "开始时间", "完成时间", "工期", "备注"));
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
                    safeCell(item.getPlannedHours()),
                    safeCell(item.getRemark())
            ));
        }
    }

    private void buildRiskExportSheet(Sheet sheet, Long projectId) {
        writeSheetRow(sheet, 0, List.of("ID", "风险编号", "风险名称", "描述", "概率", "影响", "等级", "状态", "应对策略", "关联任务ID", "关联阶段"));
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

    private String safeCell(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String buildTemplateFileName(String module) {
        return ("TASK".equals(module) ? "任务导入模板" : "风险导入模板") + ".xlsx";
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
                String name = requireText(row, "name", "任务名称", "title", "标题");
                TaskEntity entity = new TaskEntity();
                LocalDateTime now = LocalDateTime.now();
                entity.setId(IdWorker.getId());
                entity.setProjectId(projectId);
                entity.setParentTaskId(parseNullableLong(findValue(row, "parenttaskid", "父任务id")));
                if (entity.getParentTaskId() != null && taskMapper.selectEntityById(projectId, entity.getParentTaskId()) == null) {
                    throw new IllegalArgumentException("parent task not found");
                }
                entity.setTaskCode("TASK" + entity.getId());
                entity.setName(name);
                entity.setDescription(nullableText(findValue(row, "description", "描述")));
                entity.setTaskType(parseTaskType(findValue(row, "tasktype", "任务类型", "type", "类型")));
                entity.setPriority(nullableText(findValue(row, "priority", "优先级")) == null ? "MEDIUM" : nullableText(findValue(row, "priority", "优先级")));
                entity.setStatus(parseTaskStatus(findValue(row, "status", "状态")));
                entity.setProgress(parseNullableDecimal(findValue(row, "progress", "进度"), BigDecimal.ZERO));
                entity.setPlannedStartDate(parseNullableDateTime(findValue(row, "plannedstartdate", "开始时间", "start", "开始日期")));
                entity.setPlannedEndDate(parseNullableDateTime(findValue(row, "plannedenddate", "完成时间", "finish", "结束日期")));
                entity.setPlannedHours(parseNullableDecimal(findValue(row, "plannedhours", "工期", "duration", "计划工时"), BigDecimal.ZERO));
                entity.setSortOrder(baseSortOrder + index + 1);
                entity.setRemark(nullableText(findValue(row, "remark", "任务模式", "mode")));
                entity.setCreatedBy(UserContextHolder.getUserId());
                entity.setCreatedAt(now);
                entity.setUpdatedBy(UserContextHolder.getUserId());
                entity.setUpdatedAt(now);
                entity.setDeleted(0);
                taskMapper.insert(entity);
                successCount++;
            } catch (Exception ex) {
                errors.add(String.format("第 %d 行任务导入失败: %s", index + 2, ex.getMessage()));
            }
        }

        return buildImportResult(successCount, errors);
    }

    private ImportResultVO importRisks(Long projectId, List<Map<String, String>> rows) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        for (int index = 0; index < rows.size(); index++) {
            Map<String, String> row = rows.get(index);
            try {
                String name = requireText(row, "name", "风险名称", "title", "标题");
                Integer probability = parseRequiredInteger(row, "probability", "概率");
                Integer impact = parseRequiredInteger(row, "impact", "影响");
                Long taskId = parseNullableLong(findValue(row, "taskid", "关联任务id"));
                if (taskId != null && taskMapper.selectEntityById(projectId, taskId) == null) {
                    throw new IllegalArgumentException("risk task not found");
                }

                RiskEntity entity = new RiskEntity();
                LocalDateTime now = LocalDateTime.now();
                entity.setId(IdWorker.getId());
                entity.setProjectId(projectId);
                entity.setRiskCode("RISK" + entity.getId());
                entity.setName(name);
                entity.setDescription(nullableText(findValue(row, "description", "描述")));
                entity.setProbability(probability);
                entity.setImpact(impact);
                entity.setLevel(parseRiskLevel(findValue(row, "level", "等级"), probability, impact));
                entity.setStatus(parseRiskStatus(findValue(row, "status", "状态")));
                entity.setResponseStrategy(nullableText(findValue(row, "responsestrategy", "应对策略")));
                entity.setTaskId(taskId);
                entity.setPhaseName(nullableText(findValue(row, "phasename", "关联阶段", "阶段")));
                entity.setOwnerId(parseNullableLong(findValue(row, "ownerid", "责任人id")));
                entity.setIdentifiedAt(now);
                entity.setCreatedBy(UserContextHolder.getUserId());
                entity.setCreatedAt(now);
                entity.setUpdatedBy(UserContextHolder.getUserId());
                entity.setUpdatedAt(now);
                entity.setDeleted(0);
                riskMapper.insert(entity);
                successCount++;
            } catch (Exception ex) {
                errors.add(String.format("第 %d 行风险导入失败: %s", index + 2, ex.getMessage()));
            }
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
        for (DateTimeFormatter formatter : List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
        )) {
            try {
                return LocalDateTime.parse(normalized, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        for (DateTimeFormatter formatter : List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd")
        )) {
            try {
                return LocalDate.parse(normalized, formatter).atTime(LocalTime.MIN);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new IllegalArgumentException("invalid datetime: " + normalized);
    }

    private String parseTaskType(String value) {
        String normalized = nullableText(value);
        if (normalized == null) {
            return TaskTypeEnum.TASK.name();
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        if ("MILESTONE".equals(upper) || "MILESTONE_TASK".equals(upper) || "里程碑".equals(normalized)) {
            return TaskTypeEnum.MILESTONE_TASK.name();
        }
        if ("SUB_TASK".equals(upper) || "SUBTASK".equals(upper) || "摘要".equals(normalized) || "子任务".equals(normalized)) {
            return TaskTypeEnum.SUB_TASK.name();
        }
        return TaskTypeEnum.TASK.name();
    }

    private String parseTaskStatus(String value) {
        String normalized = nullableText(value);
        if (normalized == null) {
            return TaskStatusEnum.TODO.name();
        }
        return switch (normalized.toUpperCase(Locale.ROOT)) {
            case "TODO", "未开始" -> TaskStatusEnum.TODO.name();
            case "IN_PROGRESS", "进行中" -> TaskStatusEnum.IN_PROGRESS.name();
            case "BLOCKED", "阻塞" -> TaskStatusEnum.BLOCKED.name();
            case "DONE", "已完成" -> TaskStatusEnum.DONE.name();
            default -> throw new IllegalArgumentException("invalid task status: " + normalized);
        };
    }

    private String parseRiskStatus(String value) {
        String normalized = nullableText(value);
        if (normalized == null) {
            return RiskStatusEnum.IDENTIFIED.name();
        }
        return switch (normalized.toUpperCase(Locale.ROOT)) {
            case "IDENTIFIED", "已识别" -> RiskStatusEnum.IDENTIFIED.name();
            case "ANALYZED", "已分析" -> RiskStatusEnum.ANALYZED.name();
            case "RESPONDING", "已响应" -> RiskStatusEnum.RESPONDING.name();
            case "CLOSED", "已关闭" -> RiskStatusEnum.CLOSED.name();
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
            if ("低".equals(normalized)) return "LOW";
            if ("中".equals(normalized)) return "MEDIUM";
            if ("高".equals(normalized)) return "HIGH";
            if ("严重".equals(normalized)) return "CRITICAL";
        }
        int score = probability * impact;
        if (score >= 16) return "CRITICAL";
        if (score >= 10) return "HIGH";
        if (score >= 5) return "MEDIUM";
        return "LOW";
    }

    private void parseExportModule(String module) {
        ExportModuleEnum.valueOf(module);
    }

    private void parseExportFormat(String format) {
        ExportFormatEnum.valueOf(format);
    }
}
