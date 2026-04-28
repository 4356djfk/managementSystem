package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.ApproveChangeRequestDto;
import com.manage.managesystem.dto.ChangeRequestQueryDto;
import com.manage.managesystem.dto.CreateChangeRequestDto;
import com.manage.managesystem.entity.ChangeRequestEntity;
import com.manage.managesystem.entity.ChangeRequestLogEntity;
import com.manage.managesystem.enums.ChangeStatusEnum;
import com.manage.managesystem.mapper.ChangeRequestMapper;
import com.manage.managesystem.vo.ChangeRequestLogVO;
import com.manage.managesystem.vo.ChangeRequestVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChangeRequestService {
    private final ChangeRequestMapper changeRequestMapper;
    private final ProjectPermissionService projectPermissionService;
    private final NotificationService notificationService;

    public ChangeRequestService(ChangeRequestMapper changeRequestMapper,
                                ProjectPermissionService projectPermissionService,
                                NotificationService notificationService) {
        this.changeRequestMapper = changeRequestMapper;
        this.projectPermissionService = projectPermissionService;
        this.notificationService = notificationService;
    }

    public PageResult<ChangeRequestVO> list(Long projectId, ChangeRequestQueryDto queryDto) {
        projectPermissionService.ensureProjectParticipant(projectId);
        List<ChangeRequestVO> list = changeRequestMapper.selectByProjectId(projectId, queryDto);
        PageResult<ChangeRequestVO> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setPage(queryDto.getPage() == null ? 1 : queryDto.getPage());
        pageResult.setPageSize(queryDto.getPageSize() == null ? list.size() : queryDto.getPageSize());
        pageResult.setTotal((long) list.size());
        return pageResult;
    }

    @Transactional
    public ChangeRequestVO create(Long projectId, CreateChangeRequestDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        if (projectPermissionService.isProjectOwner(projectId)) {
            throw new IllegalArgumentException("project owner cannot submit change requests");
        }
        LocalDateTime now = LocalDateTime.now();
        ChangeRequestEntity entity = new ChangeRequestEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setChangeCode("CR" + entity.getId());
        entity.setTitle(dto.getTitle());
        entity.setChangeType(dto.getType());
        entity.setPriority(dto.getPriority());
        entity.setStatus(ChangeStatusEnum.SUBMITTED.name());
        entity.setProposerId(UserContextHolder.getUserId());
        entity.setReason(dto.getReason());
        entity.setImpactAnalysis(dto.getImpactAnalysis());
        entity.setSubmittedAt(now);
        entity.setCreatedBy(UserContextHolder.getUserId());
        entity.setCreatedAt(now);
        entity.setUpdatedBy(UserContextHolder.getUserId());
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        changeRequestMapper.insert(entity);
        insertLog(entity.getId(), "SUBMIT", null, ChangeStatusEnum.SUBMITTED.name(), dto.getReason());
        return changeRequestMapper.selectById(entity.getId());
    }

    public ChangeRequestVO detail(Long projectId, Long id) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ChangeRequestEntity entity = ensureRequest(projectId, id);
        return changeRequestMapper.selectById(entity.getId());
    }

    @Transactional
    public ChangeRequestVO approve(Long projectId, Long id, ApproveChangeRequestDto dto) {
        projectPermissionService.ensureProjectOwner(projectId);
        ChangeRequestEntity entity = ensureRequest(projectId, id);
        Long currentUserId = projectPermissionService.requireCurrentUserId();
        if (currentUserId.equals(entity.getProposerId())) {
            throw new IllegalArgumentException("applicant cannot approve own change request");
        }
        if (!(ChangeStatusEnum.SUBMITTED.name().equals(entity.getStatus())
                || ChangeStatusEnum.UNDER_REVIEW.name().equals(entity.getStatus()))) {
            throw new IllegalArgumentException("current status does not allow approval");
        }
        String nextStatus = normalizeDecision(dto.getDecision());
        LocalDateTime now = LocalDateTime.now();
        changeRequestMapper.updateApproval(
                id,
                nextStatus,
                currentUserId,
                dto.getComment(),
                now,
                currentUserId,
                now
        );
        insertLog(id, "APPROVE", entity.getStatus(), nextStatus, dto.getComment());
        notificationService.notifyChangeRequestApprovalResult(
                entity.getProposerId(),
                projectId,
                id,
                entity.getTitle(),
                nextStatus,
                dto.getComment()
        );
        return changeRequestMapper.selectById(id);
    }

    public List<ChangeRequestLogVO> logs(Long projectId, Long id) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureRequest(projectId, id);
        return changeRequestMapper.selectLogsByRequestId(id);
    }

    private ChangeRequestEntity ensureRequest(Long projectId, Long id) {
        ChangeRequestEntity entity = changeRequestMapper.selectEntityById(id);
        if (entity == null || !projectId.equals(entity.getProjectId())) {
            throw new IllegalArgumentException("change request not found");
        }
        return entity;
    }

    private String normalizeDecision(String decision) {
        if ("APPROVED".equals(decision)) {
            return ChangeStatusEnum.APPROVED.name();
        }
        if ("REJECTED".equals(decision)) {
            return ChangeStatusEnum.REJECTED.name();
        }
        throw new IllegalArgumentException("invalid approval decision");
    }

    private void insertLog(Long changeRequestId, String action, String fromStatus, String toStatus, String comment) {
        LocalDateTime now = LocalDateTime.now();
        ChangeRequestLogEntity log = new ChangeRequestLogEntity();
        log.setId(IdWorker.getId());
        log.setChangeRequestId(changeRequestId);
        log.setAction(action);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperatorId(UserContextHolder.getUserId());
        log.setComment(comment);
        log.setCreatedAt(now);
        changeRequestMapper.insertLog(log);
    }
}
