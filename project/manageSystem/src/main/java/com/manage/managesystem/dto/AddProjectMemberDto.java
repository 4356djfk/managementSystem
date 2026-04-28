package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddProjectMemberDto {
    @NotNull
    private Long userId;

    private String projectRole;
}
