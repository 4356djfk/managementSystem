package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MeetingVO {
    private Long id;
    private String meetingType;
    private String title;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private Long hostId;
    private String hostName;
    private String location;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
