package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProjectTemplateDto {
    @NotBlank
    private String name;

    @NotBlank
    private String type;

    private String description;

    private Integer isSystem;

    private String status;
}
