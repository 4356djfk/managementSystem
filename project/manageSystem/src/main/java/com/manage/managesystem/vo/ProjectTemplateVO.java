package com.manage.managesystem.vo;

import lombok.Data;

@Data
public class ProjectTemplateVO {
    private Long id;

    private String name;

    private String type;

    private String description;

    private Integer isSystem;

    private String status;
}
