package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunicationRecordVO {
    private Long id;
    private Long meetingId;
    private String meetingTitle;
    private String recordType;
    private String title;
    private String content;
    private Long recorderId;
    private String recorderName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
