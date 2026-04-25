package com.manage.managesystem.vo;

import lombok.Data;

import java.util.List;

/**
 * WBS鑺傜偣VO
 */
@Data
public class WbsNodeVO {

    private Long id;

    private Long parentId;

    private String code;

    private String name;

    private String description;

    private Long ownerId;

    private String ownerName;

    private Integer levelNo;

    private Integer sortOrder;

    private List<WbsNodeVO> children;
}
