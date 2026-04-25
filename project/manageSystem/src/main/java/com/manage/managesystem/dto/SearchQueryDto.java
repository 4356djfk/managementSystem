package com.manage.managesystem.dto;

import lombok.Data;

@Data
public class SearchQueryDto {
    private String keyword;
    private String type;
    private Long projectId;
    private Integer page;
    private Integer pageSize;
}
