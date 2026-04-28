package com.manage.managesystem.controller;

import com.manage.managesystem.auth.RequireRole;
import com.manage.managesystem.common.response.ApiResponse;
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
import com.manage.managesystem.enums.SystemRoleEnum;
import com.manage.managesystem.service.CommunicationService;
import com.manage.managesystem.vo.CommunicationMatrixVO;
import com.manage.managesystem.vo.CommunicationRecordVO;
import com.manage.managesystem.vo.MeetingVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}")
public class CommunicationController {
    private final CommunicationService communicationService;

    public CommunicationController(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    @GetMapping("/communication-matrix")
    public ApiResponse<PageResult<CommunicationMatrixVO>> listCommunicationMatrix(@PathVariable Long projectId,
                                                                                  CommunicationMatrixQueryDto queryDto) {
        return ApiResponse.success(communicationService.listCommunicationMatrix(projectId, queryDto));
    }

    @PostMapping("/communication-matrix")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<CommunicationMatrixVO> createCommunicationMatrix(@PathVariable Long projectId,
                                                                        @Valid @RequestBody CreateCommunicationMatrixDto dto) {
        return ApiResponse.success(communicationService.createCommunicationMatrix(projectId, dto));
    }

    @PutMapping("/communication-matrix/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<CommunicationMatrixVO> updateCommunicationMatrix(@PathVariable Long projectId,
                                                                        @PathVariable Long id,
                                                                        @Valid @RequestBody UpdateCommunicationMatrixDto dto) {
        return ApiResponse.success(communicationService.updateCommunicationMatrix(projectId, id, dto));
    }

    @DeleteMapping("/communication-matrix/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> deleteCommunicationMatrix(@PathVariable Long projectId, @PathVariable Long id) {
        communicationService.deleteCommunicationMatrix(projectId, id);
        return ApiResponse.success(null);
    }

    @GetMapping("/meetings")
    public ApiResponse<PageResult<MeetingVO>> listMeetings(@PathVariable Long projectId, MeetingQueryDto queryDto) {
        return ApiResponse.success(communicationService.listMeetings(projectId, queryDto));
    }

    @PostMapping("/meetings")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<MeetingVO> createMeeting(@PathVariable Long projectId, @Valid @RequestBody CreateMeetingDto dto) {
        return ApiResponse.success(communicationService.createMeeting(projectId, dto));
    }

    @PutMapping("/meetings/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<MeetingVO> updateMeeting(@PathVariable Long projectId,
                                                @PathVariable Long id,
                                                @Valid @RequestBody UpdateMeetingDto dto) {
        return ApiResponse.success(communicationService.updateMeeting(projectId, id, dto));
    }

    @DeleteMapping("/meetings/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> deleteMeeting(@PathVariable Long projectId, @PathVariable Long id) {
        communicationService.deleteMeeting(projectId, id);
        return ApiResponse.success(null);
    }

    @GetMapping("/communication-records")
    public ApiResponse<PageResult<CommunicationRecordVO>> listCommunicationRecords(@PathVariable Long projectId,
                                                                                   CommunicationRecordQueryDto queryDto) {
        return ApiResponse.success(communicationService.listCommunicationRecords(projectId, queryDto));
    }

    @PostMapping("/communication-records")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<CommunicationRecordVO> createCommunicationRecord(@PathVariable Long projectId,
                                                                        @Valid @RequestBody CreateCommunicationRecordDto dto) {
        return ApiResponse.success(communicationService.createCommunicationRecord(projectId, dto));
    }

    @PutMapping("/communication-records/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<CommunicationRecordVO> updateCommunicationRecord(@PathVariable Long projectId,
                                                                        @PathVariable Long id,
                                                                        @Valid @RequestBody UpdateCommunicationRecordDto dto) {
        return ApiResponse.success(communicationService.updateCommunicationRecord(projectId, id, dto));
    }

    @DeleteMapping("/communication-records/{id}")
    @RequireRole({SystemRoleEnum.SYS_ADMIN, SystemRoleEnum.USER})
    public ApiResponse<Void> deleteCommunicationRecord(@PathVariable Long projectId, @PathVariable Long id) {
        communicationService.deleteCommunicationRecord(projectId, id);
        return ApiResponse.success(null);
    }
}
