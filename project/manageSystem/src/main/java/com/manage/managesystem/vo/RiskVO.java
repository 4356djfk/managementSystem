package com.manage.managesystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 椋庨櫓VO
 */
@Data
public class RiskVO {

    private Long id;

    private String riskCode;

    private String name;

    private String description;

    private Integer probability;

    private Integer impact;

    private String level;

    private String status;

    private String responseStrategy;

    private Long taskId;

    private String taskName;

    private String phaseName;

    private Long ownerId;

    private String ownerName;

    private LocalDateTime identifiedAt;
}
