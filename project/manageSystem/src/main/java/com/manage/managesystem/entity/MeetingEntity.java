package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class MeetingEntity extends BaseEntity {
    private Long projectId;
    private String meetingType;
    private String title;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private Long hostId;
    private String location;
    private String status;
}
