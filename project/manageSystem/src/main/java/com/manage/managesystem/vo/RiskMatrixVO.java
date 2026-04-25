package com.manage.managesystem.vo;

import lombok.Data;

import java.util.List;

@Data
public class RiskMatrixVO {
    private List<RiskMatrixPointVO> levels;

    private Integer highCount;

    private Integer criticalCount;
}
