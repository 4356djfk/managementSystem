package com.manage.managesystem.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * EVM鎸ｅ€煎垎鏋愭寚鏍嘨O
 */
@Data
public class EvmMetricVO {

    private BigDecimal pv;

    private BigDecimal ev;

    private BigDecimal ac;

    private BigDecimal cv;

    private BigDecimal sv;

    private BigDecimal cpi;

    private BigDecimal spi;
}
