package com.example.card.controller;

import com.example.card.entity.UserInfo;
import com.example.card.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestParam String studentId,
            @RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
        UserInfo user = userService.login(studentId, password);
        if (user != null) {
            result.put("code", 200);
            result.put("msg", "登录成功");
            result.put("data", user);
        } else {
            result.put("code", 401);
            result.put("msg", "学号或密码错误");
        }
        return result;
    }
}