package com.manage.managesystem.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ContractVO {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private String contractNo;
    private String contractName;
    private String contractType;
    private LocalDate signDate;
    private BigDecimal totalAmount;
    private String deliverables;
    private String paymentTerms;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
