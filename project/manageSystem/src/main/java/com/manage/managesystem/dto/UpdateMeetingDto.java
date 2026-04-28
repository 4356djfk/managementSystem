package com.manage.managesystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateMeetingDto {
    @NotBlank
    private String meetingType;

    @NotBlank
    private String title;

    @NotNull
    private LocalDateTime scheduledAt;

    @Min(0)
    private Integer durationMinutes;

    private Long hostId;
    private String location;
    private String status;
}
