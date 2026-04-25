package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserDto {
    @NotBlank
    private String realName;

    private String email;

    private String phone;

    private String avatarUrl;

    private List<String> roleCodes;
}
