package com.manage.managesystem.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TimesheetQueryDto {
    private Long userId;
    private Long taskId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer page = 1;
    private Integer pageSize = 10;
}
