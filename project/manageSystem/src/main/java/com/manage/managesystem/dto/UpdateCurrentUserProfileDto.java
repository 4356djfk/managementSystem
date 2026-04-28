package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCurrentUserProfileDto {
    @NotBlank
    private String username;

    @NotBlank
    private String realName;

    private String email;

    private String phone;
}
