package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChangeRequestLogVO {
    private Long id;

    private Long changeRequestId;

    private String action;

    private String fromStatus;

    private String toStatus;

    private Long operatorId;

    private String operatorName;

    private String comment;

    private LocalDateTime createdAt;
}
