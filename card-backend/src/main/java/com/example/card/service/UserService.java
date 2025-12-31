package com.example.card.service;

import com.example.card.dao.UserDao;
import com.example.card.entity.UserInfo;
import com.example.card.util.BCryptUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;

@Service
public class UserService {
    @Resource
    private UserDao userDao;
    @Resource
    private BCryptUtil bcryptUtil;

    public UserInfo login(String studentId, String password) {
        UserInfo user = userDao.getUserByStudentId(studentId);
        if (user != null && bcryptUtil.verify(password, user.getPassword())) {
            return user;
        }
        return null;
    }
    
    public Integer getUserIdByToken(String token) {
        // 简单的token验证，实际项目中应该使用JWT等更安全的方式
        // 这里假设token格式为"userId_timestamp"或直接是userId的字符串
        try {
            if (token != null && !token.isEmpty()) {
                // 如果token是纯数字，直接作为userId
                if (token.matches("\\d+")) {
                    return Integer.parseInt(token);
                }
                // 如果token包含下划线，尝试解析第一部分作为userId
                if (token.contains("_")) {
                    String[] parts = token.split("_");
                    if (parts.length > 0 && parts[0].matches("\\d+")) {
                        return Integer.parseInt(parts[0]);
                    }
                }
            }
        } catch (NumberFormatException e) {
            // 忽略解析错误
        }
        return null;
    }
}