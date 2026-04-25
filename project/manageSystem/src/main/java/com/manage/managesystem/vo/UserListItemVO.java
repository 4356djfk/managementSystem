package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 鐢ㄦ埛鍒楄〃椤筕O
 */
@Data
public class UserListItemVO {

    private Long id;

    private String username;

    private String realName;

    private String email;

    private String phone;

    private String status;

    private List<String> roles;

    private LocalDateTime lastLoginAt;
}
