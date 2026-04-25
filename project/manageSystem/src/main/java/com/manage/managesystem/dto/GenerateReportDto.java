package com.manage.managesystem.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
public class GenerateReportDto {
    @NotBlank
    private String type;

    private LocalDate startDate;

    private LocalDate endDate;
}
