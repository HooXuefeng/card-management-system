package com.example.card;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 测试密码
        String rawPassword = "admin";
        
        // 验证数据库中的哈希值
        String dbPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        boolean matches = encoder.matches(rawPassword, dbPassword);
        
        System.out.println("密码: " + rawPassword);
        System.out.println("数据库哈希值: " + dbPassword);
        System.out.println("验证结果: " + matches);
        
        // 生成新的哈希值并验证
        String newHash = encoder.encode(rawPassword);
        boolean matchesNew = encoder.matches(rawPassword, newHash);
        System.out.println("\n新生成的哈希值: " + newHash);
        System.out.println("验证新哈希值: " + matchesNew);
        
        // 检查两个哈希值是否相同
        System.out.println("\n两个哈希值是否相同: " + newHash.equals(dbPassword));
        System.out.println("数据库哈希值长度: " + dbPassword.length());
        System.out.println("新哈希值长度: " + newHash.length());
        
        // 检查是否有不可见字符
        System.out.println("\n数据库哈希值字符逐个检查:");
        for (int i = 0; i < dbPassword.length(); i++) {
            char c = dbPassword.charAt(i);
            System.out.println("索引 " + i + ": '" + c + "' (ASCII: " + (int) c + ")");
        }
    }
}