package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProcurementEntity extends BaseEntity {
    private Long projectId;
    private Long supplierId;
    private Long costPlanId;
    private String itemName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String status;
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
}
