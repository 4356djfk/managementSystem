package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateTaskProgressDto {
    @NotNull
    private BigDecimal progress;

    @NotBlank
    private String status;

    private String remark;
}
