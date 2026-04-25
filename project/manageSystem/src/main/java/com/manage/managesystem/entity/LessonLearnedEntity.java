package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LessonLearnedEntity extends BaseEntity {
    private Long projectId;
    private String category;
    private String title;
    private String content;
    private Long authorId;
}
