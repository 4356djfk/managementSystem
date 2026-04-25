package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 鍙樻洿璇锋眰VO
 */
@Data
public class ChangeRequestVO {

    private Long id;

    private String changeCode;

    private String title;

    private String changeType;

    private String priority;

    private String status;

    private Long proposerId;

    private String proposerName;

    private Long approverId;

    private String approverName;

    private String reason;

    private String impactAnalysis;

    private String decisionComment;

    private LocalDateTime submittedAt;

    private LocalDateTime decidedAt;
}
