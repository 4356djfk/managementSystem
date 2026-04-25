package com.manage.managesystem.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GanttTaskVO {
    private Long id;

    private Long parentId;

    private String name;

    private LocalDateTime start;

    private LocalDateTime end;

    private BigDecimal progress;

    private String type;
}
