package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.dto.CreateArchiveItemDto;
import com.manage.managesystem.dto.CreateLessonLearnedDto;
import com.manage.managesystem.dto.GenerateReportDto;
import com.manage.managesystem.dto.UpdateLessonLearnedDto;
import com.manage.managesystem.entity.ArchiveItemEntity;
import com.manage.managesystem.entity.LessonLearnedEntity;
import com.manage.managesystem.entity.ReportRecordEntity;
import com.manage.managesystem.entity.AttachmentEntity;
import com.manage.managesystem.mapper.OpsMapper;
import com.manage.managesystem.mapper.ProjectClosureMapper;
import com.manage.managesystem.mapper.ProjectMapper;
import com.manage.managesystem.vo.ArchiveItemVO;
import com.manage.managesystem.vo.AttachmentVO;
import com.manage.managesystem.vo.LessonLearnedVO;
import com.manage.managesystem.vo.ReportVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProjectClosureService {
    private final ProjectClosureMapper projectClosureMapper;
    private final ProjectMapper projectMapper;
    private final OpsMapper opsMapper;
    private final ObjectMapper objectMapper;

    public ProjectClosureService(ProjectClosureMapper projectClosureMapper, ProjectMapper projectMapper, OpsMapper opsMapper, ObjectMapper objectMapper) {
        this.projectClosureMapper = projectClosureMapper;
        this.projectMapper = projectMapper;
        this.opsMapper = opsMapper;
        this.objectMapper = objectMapper;
    }

    public List<ArchiveItemVO> listArchives(Long projectId) {
        ensureProject(projectId);
        return projectClosureMapper.selectArchivesByProjectId(projectId);
    }

    @Transactional
    public ArchiveItemVO createArchive(Long projectId, CreateArchiveItemDto dto) {
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
        ensureProject(projectId);
        boolean exists = projectClosureMapper.selectArchivesByProjectId(projectId).stream()
                .anyMatch(item -> item.getId().equals(id));
        if (!exists) {
            throw new IllegalArgumentException("archive item not found");
        }
        projectClosureMapper.softDeleteArchive(projectId, id, LocalDateTime.now());
    }

    public List<LessonLearnedVO> listLessons(Long projectId) {
        ensureProject(projectId);
        return projectClosureMapper.selectLessonsByProjectId(projectId);
    }

    @Transactional
    public LessonLearnedVO createLesson(Long projectId, CreateLessonLearnedDto dto) {
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
        ensureProject(projectId);
        requireLessonEntity(projectId, id);
        projectClosureMapper.softDeleteLesson(projectId, id, LocalDateTime.now());
    }

    public List<ReportVO> listReports(Long projectId) {
        ensureProject(projectId);
        return projectClosureMapper.selectReportsByProjectId(projectId);
    }

    @Transactional
    public ReportVO generateReport(Long projectId, GenerateReportDto dto, String reportType) {
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

    private String buildTitle(String reportType, GenerateReportDto dto) {
        if ("SUMMARY".equals(reportType)) {
            return "项目总结报告";
        }
        String type = dto.getType() == null || dto.getType().isBlank() ? "WEEKLY" : dto.getType();
        return "MONTHLY".equals(type) ? "项目月报" : "项目周报";
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
            return objectMapper.writeValueAsString(Map.of(
                    "projectId", projectId,
                    "reportType", reportType,
                    "periodType", dto.getType(),
                    "startDate", dto.getStartDate(),
                    "endDate", dto.getEndDate(),
                    "generatedAt", LocalDateTime.now()
            ));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to generate report content", e);
        }
    }
}
