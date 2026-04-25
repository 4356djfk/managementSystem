package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateLessonLearnedDto {
    @NotBlank
    private String category;

    @NotBlank
    private String title;

    @NotBlank
    private String content;
}
