package com.manage.managesystem.vo;

import lombok.Data;

import java.util.List;

/**
 * 鐢ㄦ埛涓汉淇℃伅VO
 */
@Data
public class UserProfileVO {

    private Long id;

    private String username;

    private String realName;

    private String email;

    private String phone;

    private String avatarUrl;

    private String status;

    private List<String> roles;
}
