package com.manage.managesystem.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CreateCostPlanDto {
    @NotBlank
    private String type;

    @NotBlank
    private String name;

    private Long taskId; // 鍏宠仈浠诲姟ID

    private String phase; // 鎵€灞為樁锟?

    @NotNull
    private BigDecimal plannedAmount;

    @NotBlank
    private String currency;

    private String remark; // 澶囨敞
}
