package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QualityPlanVO {
    private Long id;
    private String title;
    private String qualityStandard;
    private String acceptanceRule;
    private Long ownerId;
    private String ownerName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
