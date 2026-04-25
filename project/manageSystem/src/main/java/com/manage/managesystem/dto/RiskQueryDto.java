package com.manage.managesystem.dto;

import lombok.Data;

@Data
public class RiskQueryDto {
    private String level;
    private String status;
    private Long ownerId;
    private Long taskId;
    private Integer page = 1;
    private Integer pageSize = 10;
}
