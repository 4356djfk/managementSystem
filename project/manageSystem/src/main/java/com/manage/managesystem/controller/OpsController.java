package com.manage.managesystem.controller;

import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.AuditLogQueryDto;
import com.manage.managesystem.dto.CreateAcceptanceItemDto;
import com.manage.managesystem.dto.ExportDataDto;
import com.manage.managesystem.dto.SearchQueryDto;
import com.manage.managesystem.dto.UpdateAcceptanceItemDto;
import com.manage.managesystem.service.OpsService;
import com.manage.managesystem.vo.AcceptanceItemVO;
import com.manage.managesystem.vo.AuditLogVO;
import com.manage.managesystem.vo.ExportTaskVO;
import com.manage.managesystem.vo.ImportResultVO;
import com.manage.managesystem.vo.NotificationVO;
import com.manage.managesystem.vo.SearchResultVO;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class OpsController {
    private final OpsService opsService;

    public OpsController(OpsService opsService) {
        this.opsService = opsService;
    }

    @GetMapping("/projects/{projectId}/acceptance-items")
    public ApiResponse<List<AcceptanceItemVO>> acceptanceItems(@PathVariable Long projectId) {
        return ApiResponse.success(opsService.listAcceptanceItems(projectId));
    }

    @PostMapping("/projects/{projectId}/acceptance-items")
    public ApiResponse<AcceptanceItemVO> createAcceptanceItem(@PathVariable Long projectId, @Valid @RequestBody CreateAcceptanceItemDto dto) {
        return ApiResponse.success(opsService.createAcceptanceItem(projectId, dto));
    }

    @PutMapping("/projects/{projectId}/acceptance-items/{id}")
    public ApiResponse<AcceptanceItemVO> updateAcceptanceItem(@PathVariable Long projectId, @PathVariable Long id, @Valid @RequestBody UpdateAcceptanceItemDto dto) {
        return ApiResponse.success(opsService.updateAcceptanceItem(projectId, id, dto));
    }

    @DeleteMapping("/projects/{projectId}/acceptance-items/{id}")
    public ApiResponse<Void> deleteAcceptanceItem(@PathVariable Long projectId, @PathVariable Long id) {
        opsService.deleteAcceptanceItem(projectId, id);
        return ApiResponse.success(null);
    }

    @GetMapping("/notifications")
    public ApiResponse<PageResult<NotificationVO>> notifications() {
        return ApiResponse.success(opsService.listNotifications());
    }

    @PatchMapping("/notifications/{id}/read")
    public ApiResponse<Void> readNotification(@PathVariable Long id) {
        opsService.markRead(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/notifications/read-all")
    public ApiResponse<Void> readAllNotifications() {
        opsService.markAllRead();
        return ApiResponse.success(null);
    }

    @PostMapping("/exports")
    public ApiResponse<ExportTaskVO> export(@Valid @RequestBody ExportDataDto dto) {
        return ApiResponse.success(opsService.createExportTask(dto));
    }

    @PostMapping("/imports/excel")
    public ApiResponse<ImportResultVO> importExcel(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("module") String module,
                                                   @RequestParam("projectId") Long projectId) {
        return ApiResponse.success(opsService.importExcel(file, module, projectId));
    }

    @GetMapping("/imports/excel/template")
    public ResponseEntity<Resource> downloadImportTemplate(@RequestParam("module") String module) {
        var template = opsService.downloadImportTemplate(module);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + template.fileName() + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new ByteArrayResource(template.content()));
    }

    @GetMapping("/search")
    public ApiResponse<PageResult<SearchResultVO>> search(SearchQueryDto query) {
        return ApiResponse.success(opsService.search(query));
    }

    @GetMapping("/audit-logs")
    public ApiResponse<PageResult<AuditLogVO>> auditLogs(AuditLogQueryDto query) {
        return ApiResponse.success(opsService.auditLogs(query));
    }
}
