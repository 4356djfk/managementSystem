package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 璇勮VO
 */
@Data
public class CommentVO {

    private Long id;

    private Long projectId;

    private Long taskId;

    private Long userId;

    private String userName;

    private String content;

    private Long replyToId;

    private String replyToName;

    private LocalDateTime createdAt;
}
