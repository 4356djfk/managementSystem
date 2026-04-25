package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProjectMemberDto {
    @NotBlank
    private String projectRole;

    private String memberStatus;
}
