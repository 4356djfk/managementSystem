package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class ContractEntity extends BaseEntity {
    private Long projectId;
    private Long supplierId;
    private String contractNo;
    private String contractName;
    private String contractType;
    private LocalDate signDate;
    private BigDecimal totalAmount;
    private String deliverables;
    private String paymentTerms;
    private String status;
}
