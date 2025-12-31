package com.example.card.dto;

import lombok.Data;

// 用于接收前端传递的登录参数
@Data // lombok注解，自动生成get/set方法
public class LoginDTO {
    // 字段名要和前端一致：studentId、password
    private String studentId;
    private String password;
}