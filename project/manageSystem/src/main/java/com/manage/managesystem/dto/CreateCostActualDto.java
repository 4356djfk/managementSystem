package com.manage.managesystem.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateCostActualDto {
    private Long taskId;

    @NotBlank
    private String sourceType;

    @NotNull
    private BigDecimal amount;

    @NotBlank
    private String currency;

    @NotNull
    private LocalDate spendDate;

    private String remark;
}
