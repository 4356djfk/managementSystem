package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVO {
    private Long id;

    private Long receiverId;

    private Long projectId;

    private String bizType;

    private Long bizId;

    private String title;

    private String content;

    private String readStatus;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;
}
