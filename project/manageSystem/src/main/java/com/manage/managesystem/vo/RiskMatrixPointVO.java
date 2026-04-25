package com.manage.managesystem.vo;

import lombok.Data;

@Data
public class RiskMatrixPointVO {
    private Long riskId;

    private String riskName;

    private Integer probability;

    private Integer impact;

    private String level;
}
