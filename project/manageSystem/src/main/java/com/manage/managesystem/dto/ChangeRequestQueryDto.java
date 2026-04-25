package com.manage.managesystem.dto;

import lombok.Data;

@Data
public class ChangeRequestQueryDto {
    private String status;
    private String priority;
    private Long proposerId;
    private Integer page;
    private Integer pageSize;
}
