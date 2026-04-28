package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommunicationRecordEntity extends BaseEntity {
    private Long projectId;
    private Long meetingId;
    private String recordType;
    private String title;
    private String content;
    private Long recorderId;
}
