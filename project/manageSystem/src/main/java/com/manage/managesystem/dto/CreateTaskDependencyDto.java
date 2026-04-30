package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTaskDependencyDto {
    @NotNull
    private Long predecessorTaskId;

    @NotNull
    private Long successorTaskId;

    @NotBlank
    private String dependencyType;

    private Integer lagDays;
}
