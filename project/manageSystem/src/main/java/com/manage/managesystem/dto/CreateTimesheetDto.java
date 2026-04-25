package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateTimesheetDto {
    @NotNull
    private Long taskId;

    @NotNull
    private LocalDate workDate;

    @NotNull
    private BigDecimal hours;

    private String description;
}
