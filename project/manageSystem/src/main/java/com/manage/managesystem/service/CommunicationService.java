package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.common.response.PageResult;
import com.manage.managesystem.dto.CommunicationMatrixQueryDto;
import com.manage.managesystem.dto.CommunicationRecordQueryDto;
import com.manage.managesystem.dto.CreateCommunicationMatrixDto;
import com.manage.managesystem.dto.CreateCommunicationRecordDto;
import com.manage.managesystem.dto.CreateMeetingDto;
import com.manage.managesystem.dto.MeetingQueryDto;
import com.manage.managesystem.dto.UpdateCommunicationMatrixDto;
import com.manage.managesystem.dto.UpdateCommunicationRecordDto;
import com.manage.managesystem.dto.UpdateMeetingDto;
import com.manage.managesystem.entity.CommunicationMatrixEntity;
import com.manage.managesystem.entity.CommunicationRecordEntity;
import com.manage.managesystem.entity.MeetingEntity;
import com.manage.managesystem.entity.ProjectEntity;
import com.manage.managesystem.enums.CommunicationChannelEnum;
import com.manage.managesystem.enums.CommunicationRecordTypeEnum;
import com.manage.managesystem.enums.MeetingStatusEnum;
import com.manage.managesystem.enums.MeetingTypeEnum;
import com.manage.managesystem.mapper.CommunicationMapper;
import com.manage.managesystem.mapper.ProjectMapper;
import com.manage.managesystem.vo.CommunicationMatrixVO;
import com.manage.managesystem.vo.CommunicationRecordVO;
import com.manage.managesystem.vo.MeetingVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommunicationService {
    private final CommunicationMapper communicationMapper;
    private final ProjectMapper projectMapper;
    private final ProjectPermissionService projectPermissionService;

    public CommunicationService(CommunicationMapper communicationMapper,
                                ProjectMapper projectMapper,
                                ProjectPermissionService projectPermissionService) {
        this.communicationMapper = communicationMapper;
        this.projectMapper = projectMapper;
        this.projectPermissionService = projectPermissionService;
    }

    public PageResult<CommunicationMatrixVO> listCommunicationMatrix(Long projectId, CommunicationMatrixQueryDto queryDto) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProject(projectId);
        return buildPageResult(
                communicationMapper.selectCommunicationMatrixByProjectId(projectId, queryDto),
                queryDto.getPage(),
                queryDto.getPageSize()
        );
    }

    @Transactional
    public CommunicationMatrixVO createCommunicationMatrix(Long projectId, CreateCommunicationMatrixDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        LocalDateTime now = LocalDateTime.now();
        CommunicationMatrixEntity entity = new CommunicationMatrixEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setSenderRole(dto.getSenderRole().trim());
        entity.setReceiverRole(dto.getReceiverRole().trim());
        entity.setChannel(normalizeChannel(dto.getChannel()));
        entity.setFrequency(normalizeText(dto.getFrequency()));
        entity.setTopic(normalizeText(dto.getTopic()));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        communicationMapper.insertCommunicationMatrix(entity);
        return requireCommunicationMatrix(projectId, entity.getId());
    }

    @Transactional
    public CommunicationMatrixVO updateCommunicationMatrix(Long projectId, Long id, UpdateCommunicationMatrixDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        CommunicationMatrixEntity entity = requireCommunicationMatrixEntity(projectId, id);
        entity.setSenderRole(dto.getSenderRole().trim());
        entity.setReceiverRole(dto.getReceiverRole().trim());
        entity.setChannel(normalizeChannel(dto.getChannel()));
        entity.setFrequency(normalizeText(dto.getFrequency()));
        entity.setTopic(normalizeText(dto.getTopic()));
        entity.setUpdatedAt(LocalDateTime.now());
        communicationMapper.updateCommunicationMatrix(entity);
        return requireCommunicationMatrix(projectId, id);
    }

    @Transactional
    public void deleteCommunicationMatrix(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        requireCommunicationMatrixEntity(projectId, id);
        communicationMapper.deleteCommunicationMatrix(projectId, id);
    }

    public PageResult<MeetingVO> listMeetings(Long projectId, MeetingQueryDto queryDto) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProject(projectId);
        return buildPageResult(
                communicationMapper.selectMeetingsByProjectId(projectId, queryDto),
                queryDto.getPage(),
                queryDto.getPageSize()
        );
    }

    @Transactional
    public MeetingVO createMeeting(Long projectId, CreateMeetingDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        validateProjectUser(projectId, dto.getHostId());
        LocalDateTime now = LocalDateTime.now();
        MeetingEntity entity = new MeetingEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setMeetingType(normalizeMeetingType(dto.getMeetingType()));
        entity.setTitle(dto.getTitle().trim());
        entity.setScheduledAt(dto.getScheduledAt());
        entity.setDurationMinutes(dto.getDurationMinutes());
        entity.setHostId(dto.getHostId());
        entity.setLocation(normalizeText(dto.getLocation()));
        entity.setStatus(normalizeMeetingStatus(dto.getStatus()));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        communicationMapper.insertMeeting(entity);
        return requireMeeting(projectId, entity.getId());
    }

    @Transactional
    public MeetingVO updateMeeting(Long projectId, Long id, UpdateMeetingDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        validateProjectUser(projectId, dto.getHostId());
        MeetingEntity entity = requireMeetingEntity(projectId, id);
        entity.setMeetingType(normalizeMeetingType(dto.getMeetingType()));
        entity.setTitle(dto.getTitle().trim());
        entity.setScheduledAt(dto.getScheduledAt());
        entity.setDurationMinutes(dto.getDurationMinutes());
        entity.setHostId(dto.getHostId());
        entity.setLocation(normalizeText(dto.getLocation()));
        entity.setStatus(normalizeMeetingStatus(dto.getStatus()));
        entity.setUpdatedAt(LocalDateTime.now());
        communicationMapper.updateMeeting(entity);
        return requireMeeting(projectId, id);
    }

    @Transactional
    public void deleteMeeting(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        requireMeetingEntity(projectId, id);
        if (communicationMapper.countCommunicationRecordsByMeetingId(projectId, id) > 0) {
            throw new IllegalArgumentException("该会议下还有沟通记录，请先删除相关沟通记录后再删除会议");
        }
        communicationMapper.deleteMeeting(projectId, id);
    }

    public PageResult<CommunicationRecordVO> listCommunicationRecords(Long projectId, CommunicationRecordQueryDto queryDto) {
        projectPermissionService.ensureProjectParticipant(projectId);
        ensureProject(projectId);
        return buildPageResult(
                communicationMapper.selectCommunicationRecordsByProjectId(projectId, queryDto),
                queryDto.getPage(),
                queryDto.getPageSize()
        );
    }

    @Transactional
    public CommunicationRecordVO createCommunicationRecord(Long projectId, CreateCommunicationRecordDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        validateMeeting(projectId, dto.getMeetingId());
        Long recorderId = dto.getRecorderId() != null ? dto.getRecorderId() : projectPermissionService.requireCurrentUserId();
        validateProjectUser(projectId, recorderId);
        LocalDateTime now = LocalDateTime.now();
        CommunicationRecordEntity entity = new CommunicationRecordEntity();
        entity.setId(IdWorker.getId());
        entity.setProjectId(projectId);
        entity.setMeetingId(dto.getMeetingId());
        entity.setRecordType(normalizeRecordType(dto.getRecordType()));
        entity.setTitle(dto.getTitle().trim());
        entity.setContent(dto.getContent().trim());
        entity.setRecorderId(recorderId);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        communicationMapper.insertCommunicationRecord(entity);
        return requireCommunicationRecord(projectId, entity.getId());
    }

    @Transactional
    public CommunicationRecordVO updateCommunicationRecord(Long projectId, Long id, UpdateCommunicationRecordDto dto) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        validateMeeting(projectId, dto.getMeetingId());
        CommunicationRecordEntity entity = requireCommunicationRecordEntity(projectId, id);
        Long recorderId = dto.getRecorderId() != null
                ? dto.getRecorderId()
                : (entity.getRecorderId() != null ? entity.getRecorderId() : projectPermissionService.requireCurrentUserId());
        validateProjectUser(projectId, recorderId);
        entity.setMeetingId(dto.getMeetingId());
        entity.setRecordType(normalizeRecordType(dto.getRecordType()));
        entity.setTitle(dto.getTitle().trim());
        entity.setContent(dto.getContent().trim());
        entity.setRecorderId(recorderId);
        entity.setUpdatedAt(LocalDateTime.now());
        communicationMapper.updateCommunicationRecord(entity);
        return requireCommunicationRecord(projectId, id);
    }

    @Transactional
    public void deleteCommunicationRecord(Long projectId, Long id) {
        projectPermissionService.ensureProjectEditor(projectId);
        ensureProject(projectId);
        requireCommunicationRecordEntity(projectId, id);
        communicationMapper.softDeleteCommunicationRecord(projectId, id, LocalDateTime.now());
    }

    private void ensureProject(Long projectId) {
        if (projectMapper.selectEntityById(projectId) == null) {
            throw new IllegalArgumentException("project not found");
        }
    }

    private void validateProjectUser(Long projectId, Long userId) {
        if (userId == null) {
            return;
        }
        ProjectEntity project = projectPermissionService.requireProject(projectId);
        if (project.getOwnerId() != null && project.getOwnerId().equals(userId)) {
            return;
        }
        if (!projectPermissionService.isActiveParticipant(projectId, userId)) {
            throw new IllegalArgumentException("project user not found");
        }
    }

    private void validateMeeting(Long projectId, Long meetingId) {
        if (meetingId != null && communicationMapper.selectMeetingEntityById(projectId, meetingId) == null) {
            throw new IllegalArgumentException("meeting not found");
        }
    }

    private CommunicationMatrixVO requireCommunicationMatrix(Long projectId, Long id) {
        CommunicationMatrixVO vo = communicationMapper.selectCommunicationMatrixById(projectId, id);
        if (vo == null) {
            throw new IllegalArgumentException("communication matrix not found");
        }
        return vo;
    }

    private CommunicationMatrixEntity requireCommunicationMatrixEntity(Long projectId, Long id) {
        CommunicationMatrixEntity entity = communicationMapper.selectCommunicationMatrixEntityById(projectId, id);
        if (entity == null) {
            throw new IllegalArgumentException("communication matrix not found");
        }
        return entity;
    }

    private MeetingVO requireMeeting(Long projectId, Long id) {
        MeetingVO vo = communicationMapper.selectMeetingById(projectId, id);
        if (vo == null) {
            throw new IllegalArgumentException("meeting not found");
        }
        return vo;
    }

    private MeetingEntity requireMeetingEntity(Long projectId, Long id) {
        MeetingEntity entity = communicationMapper.selectMeetingEntityById(projectId, id);
        if (entity == null) {
            throw new IllegalArgumentException("meeting not found");
        }
        return entity;
    }

    private CommunicationRecordVO requireCommunicationRecord(Long projectId, Long id) {
        CommunicationRecordVO vo = communicationMapper.selectCommunicationRecordById(projectId, id);
        if (vo == null) {
            throw new IllegalArgumentException("communication record not found");
        }
        return vo;
    }

    private CommunicationRecordEntity requireCommunicationRecordEntity(Long projectId, Long id) {
        CommunicationRecordEntity entity = communicationMapper.selectCommunicationRecordEntityById(projectId, id);
        if (entity == null) {
            throw new IllegalArgumentException("communication record not found");
        }
        return entity;
    }

    private String normalizeChannel(String channel) {
        try {
            return CommunicationChannelEnum.valueOf(channel.trim().toUpperCase()).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid communication channel");
        }
    }

    private String normalizeMeetingType(String meetingType) {
        try {
            return MeetingTypeEnum.valueOf(meetingType.trim().toUpperCase()).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid meeting type");
        }
    }

    private String normalizeMeetingStatus(String status) {
        if (status == null || status.isBlank()) {
            return MeetingStatusEnum.PLANNED.name();
        }
        try {
            return MeetingStatusEnum.valueOf(status.trim().toUpperCase()).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid meeting status");
        }
    }

    private String normalizeRecordType(String recordType) {
        if (recordType == null || recordType.isBlank()) {
            return CommunicationRecordTypeEnum.MESSAGE.name();
        }
        try {
            return CommunicationRecordTypeEnum.valueOf(recordType.trim().toUpperCase()).name();
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid communication record type");
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private <T> PageResult<T> buildPageResult(List<T> list, Integer page, Integer pageSize) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setPage(page == null ? 1 : page);
        pageResult.setPageSize(pageSize == null ? list.size() : pageSize);
        pageResult.setTotal((long) list.size());
        return pageResult;
    }
}
