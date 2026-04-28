package com.manage.managesystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConfigItemEntity extends BaseEntity {
    private Long projectId;
    private String itemName;
    private String itemType;
    private String versionNo;
    private String status;
    private String repositoryUrl;
    private String remark;
}
