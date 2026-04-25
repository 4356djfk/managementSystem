package com.manage.managesystem.auth;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AuthUser {
    private Long userId;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String status;
    private List<String> roleCodes;
    private String token;
    private LocalDateTime expiresAt;
}
