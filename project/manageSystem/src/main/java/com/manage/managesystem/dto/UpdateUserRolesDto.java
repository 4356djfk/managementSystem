package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRolesDto {
    @NotEmpty
    private List<String> roleCodes;
}
