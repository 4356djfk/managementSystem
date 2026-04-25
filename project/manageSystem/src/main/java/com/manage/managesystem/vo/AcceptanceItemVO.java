package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AcceptanceItemVO {
    private Long id;

    private Long projectId;

    private String itemName;

    private String itemType;

    private String description;

    private String status;

    private Long ownerId;

    private String ownerName;

    private Long attachmentId;

    private LocalDateTime acceptedAt;
}
