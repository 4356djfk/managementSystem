package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateArchiveItemDto {
    @NotBlank
    private String itemName;

    @NotBlank
    private String archiveType;

    private Long attachmentId;

    private String repositoryUrl;

    private String status;
}
