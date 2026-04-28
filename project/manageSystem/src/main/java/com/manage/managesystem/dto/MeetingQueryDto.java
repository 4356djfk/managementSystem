package com.manage.managesystem.dto;

import lombok.Data;

@Data
public class MeetingQueryDto {
    private String meetingType;
    private String status;
    private Long hostId;
    private Integer page = 1;
    private Integer pageSize = 10;
}
