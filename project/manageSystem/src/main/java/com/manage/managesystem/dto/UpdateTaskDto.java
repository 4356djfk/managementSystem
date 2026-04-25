package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateTaskDto {
    private Long parentTaskId;

    private Long wbsId;

    private Long milestoneId;

    @NotBlank
    private String name;

    private String description;

    private Long assigneeId;

    private LocalDateTime plannedStartDate;

    private LocalDateTime plannedEndDate;

    private BigDecimal plannedHours;

    @NotBlank
    private String priority;

    private String taskType;

    private String status;

    private BigDecimal progress;

    private Integer sortOrder;

    private String remark;
}
