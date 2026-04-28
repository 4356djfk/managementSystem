package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class QualityPlanEntity extends BaseEntity {
    private Long projectId;
    private String title;
    private String qualityStandard;
    private String acceptanceRule;
    private Long ownerId;
    private String status;
}
