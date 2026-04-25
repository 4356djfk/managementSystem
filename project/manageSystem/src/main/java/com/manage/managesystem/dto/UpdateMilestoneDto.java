package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateMilestoneDto {
    @NotBlank
    private String name;

    private String description;

    private LocalDateTime plannedDate;

    private LocalDateTime actualDate;

    private String status;

    private Long ownerId;
}
