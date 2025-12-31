package com.example.card.common;

import lombok.Data;

// 统一接口响应格式，方便前端解析
@Data
public class Result<T> {
    // 响应码：200成功，500失败
    private Integer code;
    // 响应消息
    private String msg;
    // 响应数据（可选）
    private T data;

    // 静态方法：快速返回成功响应
    public static <T> Result<T> success(String msg) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        return result;
    }

    // 重载：成功并返回数据
    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    // 静态方法：返回失败响应
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }
}