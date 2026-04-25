package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateUserDto {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String realName;

    private String email;

    private String phone;

    @NotEmpty
    private List<String> roleCodes;
}
