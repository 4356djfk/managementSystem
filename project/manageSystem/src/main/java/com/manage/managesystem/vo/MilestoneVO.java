package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 閲岀▼纰慥O
 */
@Data
public class MilestoneVO {

    private Long id;

    private String name;

    private String description;

    private LocalDateTime plannedDate;

    private LocalDateTime actualDate;

    private String status;

    private Long ownerId;

    private String ownerName;
}
