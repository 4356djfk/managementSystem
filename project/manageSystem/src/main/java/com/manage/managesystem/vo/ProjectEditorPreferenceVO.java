package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ProjectEditorPreferenceVO {
    private Long projectId;

    private Map<String, Object> ganttAppearance;

    private Map<String, Object> wbsConfig;

    private LocalDateTime updatedAt;
}
