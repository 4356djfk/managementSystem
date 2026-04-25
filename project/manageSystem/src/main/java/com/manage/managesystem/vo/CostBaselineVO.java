package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CostBaselineVO {
    private Long id;

    private Long projectId;

    private String baselineName;

    private String description;

    private String snapshotJson;

    private LocalDateTime publishedAt;
}
