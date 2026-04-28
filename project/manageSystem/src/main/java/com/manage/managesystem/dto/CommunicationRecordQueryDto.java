package com.manage.managesystem.dto;

import lombok.Data;

@Data
public class CommunicationRecordQueryDto {
    private String recordType;
    private Long meetingId;
    private Long recorderId;
    private Integer page = 1;
    private Integer pageSize = 10;
}
