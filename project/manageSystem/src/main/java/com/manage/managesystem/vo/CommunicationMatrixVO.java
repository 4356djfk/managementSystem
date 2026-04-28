package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunicationMatrixVO {
    private Long id;
    private String senderRole;
    private String receiverRole;
    private String channel;
    private String frequency;
    private String topic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
