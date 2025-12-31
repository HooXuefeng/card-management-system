package com.example.card.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserInfo {
    private Integer userId;
    private String studentId;
    private String userName;
    private String department;
    private String major;
    private String grade;
    private String classId;
    private String role;
    private String password;
    private Integer cardId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}