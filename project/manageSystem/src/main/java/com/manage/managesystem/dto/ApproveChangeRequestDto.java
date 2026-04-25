package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApproveChangeRequestDto {
    @NotBlank
    private String decision;

    private String comment;
}
