package com.manage.managesystem.common.response;

import lombok.Data;

import java.util.UUID;

@Data
public class ApiResponse<T> {
    private Integer code;
    private String message;
    private T data;
    private String requestId;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(0);
        response.setMessage("操作成功");
        response.setData(data);
        response.setRequestId(UUID.randomUUID().toString());
        return response;
    }

    public static <T> ApiResponse<T> fail(Integer code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setRequestId(UUID.randomUUID().toString());
        return response;
    }
}
