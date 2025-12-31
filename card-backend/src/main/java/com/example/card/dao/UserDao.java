package com.example.card.dao;

import com.example.card.entity.UserInfo;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import jakarta.annotation.Resource;

@Repository
public class UserDao {
    @Resource
    private JdbcTemplate jdbcTemplate;

    public UserInfo getUserByStudentId(String studentId) {
        String sql = "SELECT * FROM user_info WHERE student_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(UserInfo.class), studentId);
        } catch (Exception e) {
            return null;
        }
    }
    
    public int updateUser(UserInfo user) {
        String sql = "UPDATE user_info SET password = ? WHERE user_id = ?";
        return jdbcTemplate.update(sql, user.getPassword(), user.getUserId());
    }
}