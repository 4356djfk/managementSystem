package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class QualityActivityEntity extends BaseEntity {
    private Long projectId;
    private Long qualityPlanId;
    private String activityName;
    private String activityType;
    private LocalDateTime plannedDate;
    private LocalDateTime actualDate;
    private String result;
    private Long ownerId;
}
