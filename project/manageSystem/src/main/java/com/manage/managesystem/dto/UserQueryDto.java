package com.manage.managesystem.dto;

import lombok.Data;

@Data
public class UserQueryDto {
    private String keyword; // 濮撳悕/璐﹀彿妯＄硦鎼滅储
    private String status;  // 鐢ㄦ埛鐘舵€?
    private String role;    // 瑙掕壊缂栫爜
    private Integer page = 1; // 榛樿椤电爜
    private Integer pageSize = 10; // 榛樿椤靛ぇ灏?
}
