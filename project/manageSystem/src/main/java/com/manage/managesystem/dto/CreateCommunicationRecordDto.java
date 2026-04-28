package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCommunicationRecordDto {
    private Long meetingId;
    private String recordType;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private Long recorderId;
}
