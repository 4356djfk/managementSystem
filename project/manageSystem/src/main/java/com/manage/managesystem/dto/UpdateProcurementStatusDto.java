package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProcurementStatusDto {
    @NotBlank
    private String status;

    private LocalDate actualDeliveryDate;
}
