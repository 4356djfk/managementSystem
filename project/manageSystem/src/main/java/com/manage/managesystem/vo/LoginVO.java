package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginVO {
    private String token;

    private UserProfileVO user;

    private LocalDateTime expiresAt;
}
