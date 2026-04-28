package com.manage.managesystem.dto;

import lombok.Data;

@Data
public class DefectQueryDto {
    private String status;
    private String severity;
    private Long assigneeId;
    private Integer page = 1;
    private Integer pageSize = 10;
}
