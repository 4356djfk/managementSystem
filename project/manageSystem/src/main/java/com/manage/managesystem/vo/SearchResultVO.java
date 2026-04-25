package com.manage.managesystem.vo;

import lombok.Data;

@Data
public class SearchResultVO {
    private Long id;

    private String type;

    private String title;

    private String summary;

    private Long projectId;

    private String projectName;
}
