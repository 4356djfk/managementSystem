package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommunicationMatrixEntity extends BaseEntity {
    private Long projectId;
    private String senderRole;
    private String receiverRole;
    private String channel;
    private String frequency;
    private String topic;
}
