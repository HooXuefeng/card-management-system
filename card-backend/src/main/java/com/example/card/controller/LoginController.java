package com.example.card.controller;

import com.example.card.dao.UserDao;
import com.example.card.dto.LoginDTO;
import com.example.card.entity.UserInfo;
import com.example.card.util.BCryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.annotation.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/login")
public class LoginController implements ApplicationRunner {
    
    @Resource
    private UserDao userDao;
    
    @Resource
    private BCryptUtil bCryptUtil;

    @PostMapping
    public Map<String, Object> login(@RequestBody LoginDTO loginDTO) {
        Map<String, Object> result = new HashMap<>();
        System.out.println("收到登录请求: studentId=" + loginDTO.getStudentId());

        // 参数验证
        if (loginDTO.getStudentId() == null || loginDTO.getStudentId().isEmpty()) {
            result.put("code", 500);
            result.put("msg", "学号/工号不能为空");
            result.put("data", null);
            return result;
        }
        if (loginDTO.getPassword() == null || loginDTO.getPassword().isEmpty()) {
            result.put("code", 500);
            result.put("msg", "密码不能为空");
            result.put("data", null);
            return result;
        }

        try {
            // 查询用户信息
            UserInfo user = userDao.getUserByStudentId(loginDTO.getStudentId());
            
            // 如果用户不存在
            if (user == null) {
                result.put("code", 500);
                result.put("msg", "用户不存在");
                result.put("data", null);
                System.out.println("登录失败: 用户不存在");
                return result;
            }
            
            // 验证密码
            System.out.println("输入密码: " + loginDTO.getPassword());
            System.out.println("数据库密码: " + user.getPassword());
            System.out.println("密码长度: " + user.getPassword().length());
            boolean passwordValid = bCryptUtil.verify(loginDTO.getPassword(), user.getPassword());
            System.out.println("密码验证结果: " + passwordValid);
            
            if (passwordValid) {
                // 登录成功，生成token
                String token = user.getUserId() + "_" + UUID.randomUUID().toString().replace("-", "");
                
                // 构建返回的用户信息（不包含密码）
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getUserId());
                userInfo.put("studentId", user.getStudentId());
                userInfo.put("userName", user.getUserName());
                userInfo.put("department", user.getDepartment());
                userInfo.put("role", user.getRole());
                userInfo.put("cardId", user.getCardId());
                
                result.put("code", 200);
                result.put("msg", "登录成功");
                result.put("data", token);
                result.put("userInfo", userInfo);
                System.out.println("登录成功，用户: " + user.getUserName() + ", 生成token: " + token);
            } else {
                result.put("code", 500);
                result.put("msg", "账号或密码错误");
                result.put("data", null);
                System.out.println("登录失败: 账号或密码错误");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "系统错误，请稍后重试");
            result.put("data", null);
            System.out.println("登录失败: 系统错误 - " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 在应用启动时执行密码验证测试
        System.out.println("\n===== 密码验证测试 =====");
        
        // 测试直接使用BCryptPasswordEncoder
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String dbPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        
        boolean matchesDirect = encoder.matches(rawPassword, dbPassword);
        System.out.println("直接使用BCryptPasswordEncoder验证:");
        System.out.println("密码: " + rawPassword);
        System.out.println("数据库哈希值: " + dbPassword);
        System.out.println("验证结果: " + matchesDirect);
        
        // 测试使用BCryptUtil
        boolean matchesUtil = bCryptUtil.verify(rawPassword, dbPassword);
        System.out.println("\n使用BCryptUtil验证:");
        System.out.println("验证结果: " + matchesUtil);
        
        // 生成新的哈希值并验证
        String newHash = bCryptUtil.encrypt(rawPassword);
        boolean matchesNew = bCryptUtil.verify(rawPassword, newHash);
        System.out.println("\n生成新的哈希值并验证:");
        System.out.println("新哈希值: " + newHash);
        System.out.println("新哈希值长度: " + newHash.length());
        System.out.println("验证结果: " + matchesNew);
        
        // 直接更新数据库中的密码为新生成的哈希值
        UserInfo adminUser = userDao.getUserByStudentId("admin");
        if (adminUser != null) {
            adminUser.setPassword(newHash);
            int rowsUpdated = userDao.updateUser(adminUser);
            System.out.println("\n更新数据库中的密码:");
            System.out.println("更新行数: " + rowsUpdated);
            
            // 查询更新后的密码并验证
            UserInfo updatedUser = userDao.getUserByStudentId("admin");
            if (updatedUser != null) {
                String actualDbPassword = updatedUser.getPassword();
                boolean matchesActual = bCryptUtil.verify(rawPassword, actualDbPassword);
                System.out.println("\n更新后的实际密码哈希: " + actualDbPassword);
                System.out.println("验证结果: " + matchesActual);
            }
        }
        
        System.out.println("===== 测试结束 =====\n");
    }
}