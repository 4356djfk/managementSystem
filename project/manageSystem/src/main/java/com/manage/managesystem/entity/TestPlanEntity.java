package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TestPlanEntity extends BaseEntity {
    private Long projectId;
    private String title;
    private String versionNo;
    private String scopeDesc;
    private Long ownerId;
    private String status;
}
