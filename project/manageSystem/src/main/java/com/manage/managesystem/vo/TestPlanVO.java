package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestPlanVO {
    private Long id;
    private String title;
    private String versionNo;
    private String scopeDesc;
    private Long ownerId;
    private String ownerName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
