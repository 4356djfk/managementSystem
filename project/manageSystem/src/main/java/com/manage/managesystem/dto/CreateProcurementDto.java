package com.manage.managesystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateProcurementDto {
    private Long supplierId;

    private Long costPlanId;

    @NotBlank
    private String itemName;

    @NotNull
    @Min(1)
    private Integer quantity;

    private BigDecimal unitPrice;

    private String status;

    private LocalDate expectedDeliveryDate;

    private LocalDate actualDeliveryDate;
}
