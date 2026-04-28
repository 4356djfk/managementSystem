package com.manage.managesystem.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.manage.managesystem.auth.UserContextHolder;
import com.manage.managesystem.entity.NotificationEntity;
import com.manage.managesystem.entity.ProjectEntity;
import com.manage.managesystem.enums.ChangeStatusEnum;
import com.manage.managesystem.enums.NotificationReadStatusEnum;
import com.manage.managesystem.enums.ProjectMemberStatusEnum;
import com.manage.managesystem.enums.ProjectRoleEnum;
import com.manage.managesystem.mapper.OpsMapper;
import com.manage.managesystem.mapper.ProjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
public class NotificationService {
    private static final int MAX_COMMENT_LENGTH = 80;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final OpsMapper opsMapper;
    private final ProjectMapper projectMapper;

    public NotificationService(OpsMapper opsMapper, ProjectMapper projectMapper) {
        this.opsMapper = opsMapper;
        this.projectMapper = projectMapper;
    }

    public void notifyProjectMemberAdded(Long receiverId, Long projectId, Long memberId, String projectRole) {
        createNotification(
                receiverId,
                projectId,
                "PROJECT_MEMBER",
                memberId,
                "\u4f60\u5df2\u52a0\u5165\u9879\u76ee",
                "\u4f60\u5df2\u52a0\u5165\u9879\u76ee\u201c%s\u201d\uff0c\u5f53\u524d\u9879\u76ee\u8eab\u4efd\uff1a%s\u3002".formatted(
                        resolveProjectName(projectId),
                        formatProjectRole(projectRole)
                )
        );
    }

    public void notifyProjectMemberUpdated(Long receiverId, Long projectId, Long memberId, String projectRole, String memberStatus) {
        if (ProjectMemberStatusEnum.REMOVED.name().equals(memberStatus)) {
            createNotification(
                    receiverId,
                    projectId,
                    "PROJECT_MEMBER",
                    memberId,
                    "\u4f60\u5df2\u88ab\u79fb\u51fa\u9879\u76ee",
                    "\u4f60\u5df2\u88ab\u79fb\u51fa\u9879\u76ee\u201c%s\u201d\uff0c\u539f\u9879\u76ee\u8eab\u4efd\uff1a%s\u3002".formatted(
                            resolveProjectName(projectId),
                            formatProjectRole(projectRole)
                    )
            );
            return;
        }
        createNotification(
                receiverId,
                projectId,
                "PROJECT_MEMBER",
                memberId,
                "\u9879\u76ee\u6210\u5458\u4fe1\u606f\u5df2\u66f4\u65b0",
                "\u4f60\u5728\u9879\u76ee\u201c%s\u201d\u4e2d\u7684\u8eab\u4efd\u5df2\u66f4\u65b0\u4e3a %s\uff0c\u72b6\u6001\uff1a%s\u3002".formatted(
                        resolveProjectName(projectId),
                        formatProjectRole(projectRole),
                        formatMemberStatus(memberStatus)
                )
        );
    }

    public void notifyTaskAssigned(Long receiverId, Long projectId, Long taskId, String taskName, boolean reassigned) {
        createNotification(
                receiverId,
                projectId,
                "TASK",
                taskId,
                reassigned ? "\u4efb\u52a1\u8d1f\u8d23\u4eba\u5df2\u8c03\u6574" : "\u4f60\u6709\u65b0\u7684\u4efb\u52a1\u5206\u914d",
                "\u9879\u76ee\u201c%s\u201d\u4e2d\u7684\u4efb\u52a1\u201c%s\u201d\u5df2\u5206\u914d\u7ed9\u4f60\u3002".formatted(
                        resolveProjectName(projectId),
                        defaultText(taskName, "\u672a\u547d\u540d\u4efb\u52a1")
                )
        );
    }

    public void notifyChangeRequestApprovalResult(Long receiverId, Long projectId, Long changeRequestId, String changeTitle, String status, String comment) {
        String summary = normalizeComment(comment);
        createNotification(
                receiverId,
                projectId,
                "CHANGE_REQUEST",
                changeRequestId,
                "\u53d8\u66f4\u7533\u8bf7\u5ba1\u6279\u7ed3\u679c",
                summary == null
                        ? "\u4f60\u63d0\u4ea4\u7684\u53d8\u66f4\u201c%s\u201d\u5df2%s\u3002".formatted(defaultText(changeTitle, "\u672a\u547d\u540d\u53d8\u66f4"), formatDecision(status))
                        : "\u4f60\u63d0\u4ea4\u7684\u53d8\u66f4\u201c%s\u201d\u5df2%s\u3002\u5ba1\u6279\u610f\u89c1\uff1a%s".formatted(
                                defaultText(changeTitle, "\u672a\u547d\u540d\u53d8\u66f4"),
                                formatDecision(status),
                                summary
                        )
        );
    }

    public void notifyRiskAssigned(Long receiverId, Long projectId, Long riskId, String riskName, String level) {
        createNotification(
                receiverId,
                projectId,
                "RISK",
                riskId,
                "\u4f60\u6709\u65b0\u7684\u98ce\u9669\u8d1f\u8d23\u9879",
                "\u9879\u76ee\u201c%s\u201d\u4e2d\u7684\u98ce\u9669\u201c%s\u201d\u5df2\u5206\u914d\u7ed9\u4f60\uff0c\u7b49\u7ea7\uff1a%s\u3002".formatted(
                        resolveProjectName(projectId),
                        defaultText(riskName, "\u672a\u547d\u540d\u98ce\u9669"),
                        formatRiskLevel(level)
                )
        );
    }

    public void notifyRiskUpdated(Long receiverId, Long projectId, Long riskId, String riskName, String level, String status) {
        createNotification(
                receiverId,
                projectId,
                "RISK",
                riskId,
                "\u98ce\u9669\u4fe1\u606f\u5df2\u66f4\u65b0",
                "\u4f60\u8d1f\u8d23\u7684\u98ce\u9669\u201c%s\u201d\u5df2\u66f4\u65b0\uff0c\u7b49\u7ea7\uff1a%s\uff0c\u72b6\u6001\uff1a%s\u3002".formatted(
                        defaultText(riskName, "\u672a\u547d\u540d\u98ce\u9669"),
                        formatRiskLevel(level),
                        formatRiskStatus(status)
                )
        );
    }

    public void notifyMilestoneAssigned(Long receiverId, Long projectId, Long milestoneId, String milestoneName, LocalDateTime plannedDate) {
        createNotification(
                receiverId,
                projectId,
                "MILESTONE",
                milestoneId,
                "\u4f60\u6709\u65b0\u7684\u91cc\u7a0b\u7891\u8d1f\u8d23\u9879",
                "\u9879\u76ee\u201c%s\u201d\u4e2d\u7684\u91cc\u7a0b\u7891\u201c%s\u201d\u5df2\u6307\u6d3e\u7ed9\u4f60\uff0c\u8ba1\u5212\u65f6\u95f4\uff1a%s\u3002".formatted(
                        resolveProjectName(projectId),
                        defaultText(milestoneName, "\u672a\u547d\u540d\u91cc\u7a0b\u7891"),
                        formatDateTime(plannedDate)
                )
        );
    }

    public void notifyMilestoneUpdated(Long receiverId, Long projectId, Long milestoneId, String milestoneName, LocalDateTime plannedDate, String status) {
        createNotification(
                receiverId,
                projectId,
                "MILESTONE",
                milestoneId,
                "\u91cc\u7a0b\u7891\u4fe1\u606f\u5df2\u66f4\u65b0",
                "\u4f60\u8d1f\u8d23\u7684\u91cc\u7a0b\u7891\u201c%s\u201d\u5df2\u66f4\u65b0\uff0c\u72b6\u6001\uff1a%s\uff0c\u8ba1\u5212\u65f6\u95f4\uff1a%s\u3002".formatted(
                        defaultText(milestoneName, "\u672a\u547d\u540d\u91cc\u7a0b\u7891"),
                        formatMilestoneStatus(status),
                        formatDateTime(plannedDate)
                )
        );
    }

    public void notifyAcceptanceItemAssigned(Long receiverId, Long projectId, Long acceptanceItemId, String itemName) {
        createNotification(
                receiverId,
                projectId,
                "ACCEPTANCE_ITEM",
                acceptanceItemId,
                "\u4f60\u6709\u65b0\u7684\u9a8c\u6536\u4e8b\u9879",
                "\u9879\u76ee\u201c%s\u201d\u4e2d\u7684\u9a8c\u6536\u4e8b\u9879\u201c%s\u201d\u5df2\u5206\u914d\u7ed9\u4f60\u3002".formatted(
                        resolveProjectName(projectId),
                        defaultText(itemName, "\u672a\u547d\u540d\u9a8c\u6536\u9879")
                )
        );
    }

    public void notifyAcceptanceItemCompleted(Long receiverId, Long projectId, Long acceptanceItemId, String itemName) {
        createNotification(
                receiverId,
                projectId,
                "ACCEPTANCE_ITEM",
                acceptanceItemId,
                "\u9a8c\u6536\u4e8b\u9879\u5df2\u5b8c\u6210",
                "\u4f60\u8d1f\u8d23\u7684\u9a8c\u6536\u4e8b\u9879\u201c%s\u201d\u5df2\u6807\u8bb0\u4e3a\u5b8c\u6210\u3002".formatted(
                        defaultText(itemName, "\u672a\u547d\u540d\u9a8c\u6536\u9879")
                )
        );
    }

    public void notifyTestCaseAssigned(Long receiverId, Long projectId, Long testCaseId, String caseTitle, String status) {
        createNotification(
                receiverId,
                projectId,
                "TEST_CASE",
                testCaseId,
                "\u4f60\u6709\u65b0\u7684\u6d4b\u8bd5\u7528\u4f8b",
                "\u9879\u76ee\u201c%s\u201d\u4e2d\u7684\u6d4b\u8bd5\u7528\u4f8b\u201c%s\u201d\u5df2\u5206\u914d\u7ed9\u4f60\uff0c\u5f53\u524d\u72b6\u6001\uff1a%s\u3002".formatted(
                        resolveProjectName(projectId),
                        defaultText(caseTitle, "\u672a\u547d\u540d\u6d4b\u8bd5\u7528\u4f8b"),
                        formatTestCaseStatus(status)
                )
        );
    }

    public void notifyTestCaseUpdated(Long receiverId, Long projectId, Long testCaseId, String caseTitle, String status) {
        createNotification(
                receiverId,
                projectId,
                "TEST_CASE",
                testCaseId,
                "\u6d4b\u8bd5\u7528\u4f8b\u72b6\u6001\u5df2\u66f4\u65b0",
                "\u4f60\u8d1f\u8d23\u7684\u6d4b\u8bd5\u7528\u4f8b\u201c%s\u201d\u5df2\u66f4\u65b0\uff0c\u5f53\u524d\u72b6\u6001\uff1a%s\u3002".formatted(
                        defaultText(caseTitle, "\u672a\u547d\u540d\u6d4b\u8bd5\u7528\u4f8b"),
                        formatTestCaseStatus(status)
                )
        );
    }

    public void notifyDefectAssigned(Long receiverId, Long projectId, Long defectId, String defectTitle, String severity, String status) {
        createNotification(
                receiverId,
                projectId,
                "DEFECT",
                defectId,
                "\u4f60\u6709\u65b0\u7684\u7f3a\u9677\u5904\u7406\u9879",
                "\u9879\u76ee\u201c%s\u201d\u4e2d\u7684\u7f3a\u9677\u201c%s\u201d\u5df2\u5206\u914d\u7ed9\u4f60\uff0c\u4e25\u91cd\u7ea7\uff1a%s\uff0c\u72b6\u6001\uff1a%s\u3002".formatted(
                        resolveProjectName(projectId),
                        defaultText(defectTitle, "\u672a\u547d\u540d\u7f3a\u9677"),
                        formatRiskLevel(severity),
                        formatDefectStatus(status)
                )
        );
    }

    public void notifyDefectUpdated(Long receiverId, Long projectId, Long defectId, String defectTitle, String severity, String status) {
        createNotification(
                receiverId,
                projectId,
                "DEFECT",
                defectId,
                "\u7f3a\u9677\u4fe1\u606f\u5df2\u66f4\u65b0",
                "\u4f60\u8d1f\u8d23\u7684\u7f3a\u9677\u201c%s\u201d\u5df2\u66f4\u65b0\uff0c\u4e25\u91cd\u7ea7\uff1a%s\uff0c\u72b6\u6001\uff1a%s\u3002".formatted(
                        defaultText(defectTitle, "\u672a\u547d\u540d\u7f3a\u9677"),
                        formatRiskLevel(severity),
                        formatDefectStatus(status)
                )
        );
    }

    public void notifyExportCompleted(Long receiverId, Long projectId, Long exportTaskId, String moduleName, String exportFormat) {
        createNotification(
                receiverId,
                projectId,
                "EXPORT_TASK",
                exportTaskId,
                "\u6570\u636e\u5bfc\u51fa\u5df2\u5b8c\u6210",
                "\u9879\u76ee\u201c%s\u201d\u7684%s\u5bfc\u51fa\u5df2\u5b8c\u6210\uff0c\u683c\u5f0f\uff1a%s\uff0c\u53ef\u5728\u5bfc\u51fa\u8bb0\u5f55\u4e2d\u4e0b\u8f7d\u6587\u4ef6\u3002".formatted(
                        resolveProjectName(projectId),
                        formatExportModule(moduleName),
                        formatExportFormat(exportFormat)
                ),
                true
        );
    }

    public void createNotification(Long receiverId, Long projectId, String bizType, Long bizId, String title, String content) {
        createNotification(receiverId, projectId, bizType, bizId, title, content, false);
    }

    private void createNotification(Long receiverId, Long projectId, String bizType, Long bizId, String title, String content, boolean allowSelf) {
        Long operatorId = UserContextHolder.getUserId();
        if (receiverId == null || (!allowSelf && Objects.equals(receiverId, operatorId))) {
            return;
        }
        NotificationEntity entity = new NotificationEntity();
        entity.setId(IdWorker.getId());
        entity.setReceiverId(receiverId);
        entity.setProjectId(projectId);
        entity.setBizType(bizType);
        entity.setBizId(bizId);
        entity.setTitle(title);
        entity.setContent(content);
        entity.setReadStatus(NotificationReadStatusEnum.UNREAD.name());
        entity.setReadAt(null);
        entity.setCreatedAt(LocalDateTime.now());
        opsMapper.insertNotification(entity);
    }

    private String resolveProjectName(Long projectId) {
        if (projectId == null) {
            return "\u5f53\u524d\u9879\u76ee";
        }
        ProjectEntity project = projectMapper.selectEntityById(projectId);
        if (project == null || project.getName() == null || project.getName().isBlank()) {
            return "\u9879\u76ee#" + projectId;
        }
        return project.getName();
    }

    private String formatProjectRole(String role) {
        if (ProjectRoleEnum.READ_ONLY.name().equals(role)) {
            return "\u53ea\u8bfb\u6210\u5458";
        }
        if (ProjectRoleEnum.TEAM_MEMBER.name().equals(role)) {
            return "\u9879\u76ee\u6210\u5458";
        }
        return defaultText(role, "\u9879\u76ee\u6210\u5458");
    }

    private String formatMemberStatus(String status) {
        if (ProjectMemberStatusEnum.ACTIVE.name().equals(status)) {
            return "\u5728\u5c97";
        }
        if (ProjectMemberStatusEnum.REMOVED.name().equals(status)) {
            return "\u5df2\u79fb\u51fa";
        }
        return defaultText(status, "\u672a\u77e5");
    }

    private String formatDecision(String status) {
        if (ChangeStatusEnum.APPROVED.name().equals(status)) {
            return "\u6279\u51c6";
        }
        if (ChangeStatusEnum.REJECTED.name().equals(status)) {
            return "\u9a73\u56de";
        }
        return defaultText(status, "\u5904\u7406");
    }

    private String normalizeComment(String comment) {
        if (comment == null || comment.isBlank()) {
            return null;
        }
        String normalized = comment.trim().replace("\r", " ").replace("\n", " ");
        if (normalized.length() <= MAX_COMMENT_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MAX_COMMENT_LENGTH) + "...";
    }

    private String defaultText(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private String formatRiskLevel(String level) {
        if (level == null || level.isBlank()) {
            return "\u672a\u8bbe\u7f6e";
        }
        return switch (level.trim().toUpperCase()) {
            case "LOW" -> "\u4f4e";
            case "MEDIUM" -> "\u4e2d";
            case "HIGH" -> "\u9ad8";
            case "CRITICAL" -> "\u4e25\u91cd";
            default -> level.trim();
        };
    }

    private String formatRiskStatus(String status) {
        if (status == null || status.isBlank()) {
            return "\u672a\u8bbe\u7f6e";
        }
        return switch (status.trim().toUpperCase()) {
            case "IDENTIFIED" -> "\u5df2\u8bc6\u522b";
            case "ANALYZED" -> "\u5df2\u5206\u6790";
            case "RESPONDING" -> "\u5e94\u5bf9\u4e2d";
            case "CLOSED" -> "\u5df2\u5173\u95ed";
            default -> status.trim();
        };
    }

    private String formatMilestoneStatus(String status) {
        if (status == null || status.isBlank()) {
            return "\u672a\u8bbe\u7f6e";
        }
        return switch (status.trim().toUpperCase()) {
            case "PENDING" -> "\u5f85\u8fbe\u6210";
            case "REACHED" -> "\u5df2\u8fbe\u6210";
            case "DELAYED" -> "\u5df2\u5ef6\u8bef";
            default -> status.trim();
        };
    }

    private String formatTestCaseStatus(String status) {
        if (status == null || status.isBlank()) {
            return "\u672a\u8bbe\u7f6e";
        }
        return switch (status.trim().toUpperCase()) {
            case "NOT_RUN" -> "\u672a\u6267\u884c";
            case "PASSED" -> "\u5df2\u901a\u8fc7";
            case "FAILED" -> "\u5df2\u5931\u8d25";
            case "BLOCKED" -> "\u88ab\u963b\u585e";
            default -> status.trim();
        };
    }

    private String formatDefectStatus(String status) {
        if (status == null || status.isBlank()) {
            return "\u672a\u8bbe\u7f6e";
        }
        return switch (status.trim().toUpperCase()) {
            case "NEW" -> "\u65b0\u5efa";
            case "PROCESSING" -> "\u5904\u7406\u4e2d";
            case "FIXED" -> "\u5df2\u4fee\u590d";
            case "CLOSED" -> "\u5df2\u5173\u95ed";
            default -> status.trim();
        };
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "\u672a\u8bbe\u7f6e";
        }
        return value.format(DATE_TIME_FORMATTER);
    }

    private String formatExportModule(String moduleName) {
        if (moduleName == null || moduleName.isBlank()) {
            return "\u6570\u636e";
        }
        return switch (moduleName.trim().toUpperCase()) {
            case "TASK" -> "\u4efb\u52a1";
            case "RISK" -> "\u98ce\u9669";
            case "COST" -> "\u6210\u672c";
            case "TIMESHEET" -> "\u5de5\u65f6";
            case "REPORT" -> "\u62a5\u8868";
            default -> moduleName.trim();
        };
    }

    private String formatExportFormat(String exportFormat) {
        if (exportFormat == null || exportFormat.isBlank()) {
            return "\u672a\u8bbe\u7f6e";
        }
        return switch (exportFormat.trim().toUpperCase()) {
            case "EXCEL" -> "Excel";
            case "PDF" -> "PDF";
            default -> exportFormat.trim();
        };
    }
}
