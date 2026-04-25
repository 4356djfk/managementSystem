package com.manage.managesystem.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

@Data
public class ExportDataDto {
    private Long projectId;

    @NotBlank
    private String module;

    @NotBlank
    private String format;

    private Map<String, Object> filters;
}
