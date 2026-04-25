package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserStatusDto {
    @NotBlank
    private String status;
}
