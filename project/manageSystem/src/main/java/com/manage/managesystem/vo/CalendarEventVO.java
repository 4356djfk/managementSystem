package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CalendarEventVO {
    private Long id;

    private String eventType;

    private String title;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long projectId;

    private Long bizId;
}
