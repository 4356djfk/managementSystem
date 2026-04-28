package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QualityActivityVO {
    private Long id;
    private Long qualityPlanId;
    private String qualityPlanTitle;
    private String activityName;
    private String activityType;
    private LocalDateTime plannedDate;
    private LocalDateTime actualDate;
    private String result;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
