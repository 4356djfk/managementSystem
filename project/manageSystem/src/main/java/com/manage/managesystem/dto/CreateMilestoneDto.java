package com.manage.managesystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateMilestoneDto {
    @NotBlank
    private String name;

    private String description;

    private LocalDateTime plannedDate;

    private Long ownerId;
}
