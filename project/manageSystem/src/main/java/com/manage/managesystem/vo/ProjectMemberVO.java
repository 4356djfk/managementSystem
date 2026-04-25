package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 椤圭洰鎴愬憳VO
 */
@Data
public class ProjectMemberVO {

    private Long id;

    private Long userId;

    private String realName;

    private String username;

    private String email;

    private String projectRole;

    private String memberStatus;

    private LocalDateTime joinedAt;
}
