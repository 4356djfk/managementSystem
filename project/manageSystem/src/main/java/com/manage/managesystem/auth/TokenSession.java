package com.manage.managesystem.auth;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenSession {
    private String token;
    private Long userId;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String status;
    private List<String> roleCodes;
    private LocalDateTime expiresAt;
}
