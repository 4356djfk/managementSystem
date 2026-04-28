package com.manage.managesystem.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SaveProjectEditorPreferenceDto {
    private Map<String, Object> ganttAppearance;

    private Map<String, Object> wbsConfig;
}
