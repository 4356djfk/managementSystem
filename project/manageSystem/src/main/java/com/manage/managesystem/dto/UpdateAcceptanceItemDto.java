package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAcceptanceItemDto {
    @NotBlank
    private String itemName;

    private String itemType;

    private String description;

    private String status;

    private Long ownerId;

    private Long attachmentId;
}
