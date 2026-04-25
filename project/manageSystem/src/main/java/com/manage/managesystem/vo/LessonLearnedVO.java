package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LessonLearnedVO {
    private Long id;

    private Long projectId;

    private String category;

    private String title;

    private String content;

    private Long authorId;

    private String authorName;

    private LocalDateTime createdAt;
}
