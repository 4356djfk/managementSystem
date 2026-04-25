package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTaskCommentDto {
    @NotBlank
    private String content;

    private Long replyToId;
}
