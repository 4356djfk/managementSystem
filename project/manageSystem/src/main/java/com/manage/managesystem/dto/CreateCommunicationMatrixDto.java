package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCommunicationMatrixDto {
    @NotBlank
    private String senderRole;

    @NotBlank
    private String receiverRole;

    @NotBlank
    private String channel;

    private String frequency;
    private String topic;
}
