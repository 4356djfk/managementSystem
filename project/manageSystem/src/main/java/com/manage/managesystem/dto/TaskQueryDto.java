package com.manage.managesystem.dto;

import lombok.Data;

@Data
public class TaskQueryDto {
    private Long assigneeId; // 璐熻矗浜篒D
    private String status;   // 浠诲姟鐘讹拷?
    private Long milestoneId; // 鍏宠仈閲岀▼纰慖D
    private String keyword;  // 浠诲姟鍚嶇О/缂栫爜妯＄硦鎼滅储
    private Integer page = 1;
    private Integer pageSize = 10;
}
