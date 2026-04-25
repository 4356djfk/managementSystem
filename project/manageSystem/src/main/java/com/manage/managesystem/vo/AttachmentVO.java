package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttachmentVO {
    private Long id;

    private String fileName;

    private String fileUrl;

    private Long fileSize;

    private String fileType;

    private String bizType;

    private Long bizId;

    private Long uploadedBy;

    private LocalDateTime uploadedAt;
}
