package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class AcceptanceItemEntity extends BaseEntity {
    private Long projectId;
    private String itemName;
    private String itemType;
    private String description;
    private String status;
    private Long ownerId;
    private Long attachmentId;
    private LocalDateTime acceptedAt;
}
