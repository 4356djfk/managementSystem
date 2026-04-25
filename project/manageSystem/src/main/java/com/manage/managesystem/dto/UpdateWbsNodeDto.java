package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateWbsNodeDto {
    private Long parentId;

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String description;

    private Long ownerId;

    private Integer sortOrder;
}
