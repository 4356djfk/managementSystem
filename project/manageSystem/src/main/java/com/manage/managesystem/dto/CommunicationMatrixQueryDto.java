package com.manage.managesystem.dto;

import lombok.Data;

@Data
public class CommunicationMatrixQueryDto {
    private String channel;
    private String senderRole;
    private String receiverRole;
    private Integer page = 1;
    private Integer pageSize = 10;
}
