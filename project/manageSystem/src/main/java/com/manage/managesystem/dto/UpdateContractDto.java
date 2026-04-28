package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateContractDto {
    private Long supplierId;

    @NotBlank
    private String contractNo;

    @NotBlank
    private String contractName;

    @NotBlank
    private String contractType;

    private LocalDate signDate;

    private BigDecimal totalAmount;

    private String deliverables;

    private String paymentTerms;

    private String status;
}
