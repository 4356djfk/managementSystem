package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目编辑器偏好，对应表：project_editor_preference
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectEditorPreferenceEntity extends BaseEntity {
    private Long projectId;

    private String ganttAppearanceJson;

    private String wbsConfigJson;
}
