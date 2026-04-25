package com.manage.managesystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProjectQueryDto {
    private String keyword; // 椤圭洰鍚嶇О/缂栫爜妯＄硦鎼滅储
    private String status;  // 椤圭洰鐘讹拷?
    private String lifeCycleModel; // 鐢熷懡鍛ㄦ湡妯″瀷
    private Long ownerId;   // 璐熻矗浜篒D
    private Integer page = 1;
    private Integer pageSize = 10;
}
