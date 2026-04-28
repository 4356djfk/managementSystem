package com.manage.managesystem.mapper;

import com.manage.managesystem.entity.AttachmentEntity;
import com.manage.managesystem.dto.AuditLogQueryDto;
import com.manage.managesystem.entity.ExportTaskEntity;
import com.manage.managesystem.entity.NotificationEntity;
import com.manage.managesystem.entity.OperationLogEntity;
import com.manage.managesystem.vo.AcceptanceItemVO;
import com.manage.managesystem.vo.AuditLogVO;
import com.manage.managesystem.vo.ExportTaskVO;
import com.manage.managesystem.vo.NotificationVO;
import com.manage.managesystem.vo.SearchResultVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OpsMapper {
    List<AcceptanceItemVO> selectAcceptanceItems(@Param("projectId") Long projectId);

    AcceptanceItemVO selectAcceptanceItemById(@Param("projectId") Long projectId, @Param("id") Long id);

    int insertAcceptanceItem(@Param("id") Long id,
                             @Param("projectId") Long projectId,
                             @Param("itemName") String itemName,
                             @Param("description") String description,
                             @Param("status") String status,
                             @Param("checkerId") Long checkerId,
                             @Param("checkedAt") LocalDateTime checkedAt,
                             @Param("remark") String remark,
                             @Param("createdAt") LocalDateTime createdAt,
                             @Param("updatedAt") LocalDateTime updatedAt);

    int updateAcceptanceItem(@Param("projectId") Long projectId,
                             @Param("id") Long id,
                             @Param("itemName") String itemName,
                             @Param("description") String description,
                             @Param("status") String status,
                             @Param("checkerId") Long checkerId,
                             @Param("checkedAt") LocalDateTime checkedAt,
                             @Param("remark") String remark,
                             @Param("updatedAt") LocalDateTime updatedAt);

    int softDeleteAcceptanceItem(@Param("projectId") Long projectId,
                                 @Param("id") Long id,
                                 @Param("updatedAt") LocalDateTime updatedAt);

    List<NotificationVO> selectNotifications(@Param("receiverId") Long receiverId);

    int insertNotification(NotificationEntity entity);

    int markNotificationRead(@Param("id") Long id, @Param("receiverId") Long receiverId, @Param("readAt") LocalDateTime readAt);

    int markAllNotificationsRead(@Param("receiverId") Long receiverId, @Param("readAt") LocalDateTime readAt);

    int insertExportTask(ExportTaskEntity entity);

    int updateExportTaskResult(@Param("id") Long id,
                               @Param("status") String status,
                               @Param("fileAttachmentId") Long fileAttachmentId,
                               @Param("finishedAt") LocalDateTime finishedAt);

    ExportTaskVO selectExportTaskById(@Param("id") Long id);

    int insertAttachment(AttachmentEntity entity);

    int insertOperationLog(OperationLogEntity entity);

    List<SearchResultVO> search(@Param("keyword") String keyword,
                                @Param("type") String type,
                                @Param("projectId") Long projectId);

    List<AuditLogVO> selectAuditLogs(@Param("query") AuditLogQueryDto query);
}
