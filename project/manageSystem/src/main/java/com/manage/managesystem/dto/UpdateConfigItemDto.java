package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateConfigItemDto {
    @NotBlank
    private String itemName;

    @NotBlank
    private String itemType;

    private String versionNo;
    private String status;
    private String repositoryUrl;
    private String remark;
}
