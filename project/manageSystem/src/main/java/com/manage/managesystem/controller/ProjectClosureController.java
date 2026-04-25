package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
import com.manage.managesystem.dto.CreateArchiveItemDto;
import com.manage.managesystem.dto.CreateLessonLearnedDto;
import com.manage.managesystem.dto.GenerateReportDto;
import com.manage.managesystem.dto.UpdateLessonLearnedDto;
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.ProjectClosureService;
import com.manage.managesystem.vo.ArchiveItemVO;
import com.manage.managesystem.vo.AttachmentVO;
import com.manage.managesystem.vo.LessonLearnedVO;
import com.manage.managesystem.vo.ReportVO;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class ProjectClosureController {
    private final ProjectClosureService projectClosureService;

    public ProjectClosureController(ProjectClosureService projectClosureService) {
        this.projectClosureService = projectClosureService;
    }

    @GetMapping("/projects/{projectId}/archives")
    public ApiResponse<List<ArchiveItemVO>> listArchives(@PathVariable Long projectId) {
        return ApiResponse.success(projectClosureService.listArchives(projectId));
    }

    @PostMapping("/projects/{projectId}/archives")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<ArchiveItemVO> createArchive(@PathVariable Long projectId, @Valid @RequestBody CreateArchiveItemDto dto) {
        return ApiResponse.success(projectClosureService.createArchive(projectId, dto));
    }

    @DeleteMapping("/projects/{projectId}/archives/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<Void> deleteArchive(@PathVariable Long projectId, @PathVariable Long id) {
        projectClosureService.deleteArchive(projectId, id);
        return ApiResponse.success(null);
    }

    @GetMapping("/projects/{projectId}/lessons-learned")
    public ApiResponse<List<LessonLearnedVO>> listLessons(@PathVariable Long projectId) {
        return ApiResponse.success(projectClosureService.listLessons(projectId));
    }

    @PostMapping("/projects/{projectId}/lessons-learned")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER, SystemRoleEnum.TEAM_MEMBER})
    public ApiResponse<LessonLearnedVO> createLesson(@PathVariable Long projectId, @Valid @RequestBody CreateLessonLearnedDto dto) {
        return ApiResponse.success(projectClosureService.createLesson(projectId, dto));
    }

    @PutMapping("/projects/{projectId}/lessons-learned/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER, SystemRoleEnum.TEAM_MEMBER})
    public ApiResponse<LessonLearnedVO> updateLesson(@PathVariable Long projectId, @PathVariable Long id, @Valid @RequestBody UpdateLessonLearnedDto dto) {
        return ApiResponse.success(projectClosureService.updateLesson(projectId, id, dto));
    }

    @DeleteMapping("/projects/{projectId}/lessons-learned/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER, SystemRoleEnum.TEAM_MEMBER})
    public ApiResponse<Void> deleteLesson(@PathVariable Long projectId, @PathVariable Long id) {
        projectClosureService.deleteLesson(projectId, id);
        return ApiResponse.success(null);
    }

    @PostMapping("/projects/{projectId}/reports/generate")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<ReportVO> generateReport(@PathVariable Long projectId, @Valid @RequestBody GenerateReportDto dto) {
        return ApiResponse.success(projectClosureService.generateReport(projectId, dto, "PERIODIC"));
    }

    @GetMapping("/projects/{projectId}/reports")
    public ApiResponse<List<ReportVO>> listReports(@PathVariable Long projectId) {
        return ApiResponse.success(projectClosureService.listReports(projectId));
    }

    @PostMapping("/projects/{projectId}/summary-report/generate")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<ReportVO> generateSummaryReport(@PathVariable Long projectId, @Valid @RequestBody GenerateReportDto dto) {
        return ApiResponse.success(projectClosureService.generateReport(projectId, dto, "SUMMARY"));
    }

    @PostMapping("/projects/{projectId}/attachments/upload")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER, SystemRoleEnum.TEAM_MEMBER})
    public ApiResponse<AttachmentVO> uploadAttachment(@PathVariable Long projectId, @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(projectClosureService.uploadAttachment(projectId, file));
    }

    @GetMapping("/attachments/{id}")
    public ApiResponse<AttachmentVO> attachmentDetail(@PathVariable Long id) {
        return ApiResponse.success(projectClosureService.attachmentDetail(id));
    }

    @GetMapping("/attachments/{id}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id) {
        AttachmentVO attachment = projectClosureService.attachmentDetail(id);
        Resource resource = new FileSystemResource(attachment.getFileUrl());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .contentType(resolveAttachmentMediaType(attachment.getFileType()))
                .body(resource);
    }

    @DeleteMapping("/attachments/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.PROJECT_MANAGER})
    public ApiResponse<Void> deleteAttachment(@PathVariable Long id) {
        projectClosureService.deleteAttachment(id);
        return ApiResponse.success(null);
    }

    private MediaType resolveAttachmentMediaType(String fileType) {
        if (fileType == null || fileType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        String normalized = fileType.trim().toLowerCase();
        return switch (normalized) {
            case "xlsx" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "xls" -> MediaType.parseMediaType("application/vnd.ms-excel");
            case "csv" -> MediaType.parseMediaType("text/csv");
            default -> normalized.contains("/") ? MediaType.parseMediaType(normalized) : MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
