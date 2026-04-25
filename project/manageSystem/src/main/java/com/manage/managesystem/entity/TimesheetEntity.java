package com.manage.managesystem.entity;

import com.manage.managesystem.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 宸ユ椂璁板綍瀹炰綋
 * 瀵瑰簲琛細timesheet
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TimesheetEntity extends BaseEntity {
    /**
     * 椤圭洰 ID
     */
    private Long projectId;

    /**
     * 浠诲姟 ID
     */
    private Long taskId;

    /**
     * 濉姤锟?
     */
    private Long userId;

    /**
     * 宸ユ椂鏃ユ湡
     */
    private LocalDate workDate;

    /**
     * 宸ユ椂
     */
    private BigDecimal hours;

    private BigDecimal costRate;

    private BigDecimal laborCost;

    /**
     * 宸ヤ綔璇存槑
     */
    private String description;

    /**
     * 宸ユ椂鐘讹拷?
     */
    private String status;
}
