package com.example.card.dao;

import com.example.card.entity.CardInfo;
import com.example.card.entity.CardOperationLog;
import com.example.card.entity.ConsumeRecord;
import com.example.card.entity.RechargeRecord;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Repository
public class CardDao {
    @Resource
    private JdbcTemplate jdbcTemplate;

    public CardInfo getCardById(Integer cardId) {
        String sql = "SELECT * FROM card_info WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(CardInfo.class), cardId);
        } catch (Exception e) {
            return null;
        }
    }
    
    public CardInfo getCardByUserId(Integer userId) {
        System.out.println("Debug - CardDao.getCardByUserId called with userId: " + userId);
        String sql = "SELECT * FROM card_info WHERE user_id = ? ORDER BY create_time DESC LIMIT 1";
        try {
            List<CardInfo> results = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CardInfo.class), userId);
            System.out.println("Debug - CardDao.getCardByUserId results count: " + results.size());
            if (!results.isEmpty()) {
                CardInfo result = results.get(0);
                System.out.println("Debug - CardDao.getCardByUserId result: " + result.toString());
                return result;
            }
            return null;
        } catch (Exception e) {
            System.out.println("Debug - CardDao.getCardByUserId exception: " + e.getMessage());
            return null;
        }
    }

    public int updateCardBalance(Integer cardId, BigDecimal newBalance) {
        String sql = "UPDATE card_info SET balance = ?, update_time = NOW() WHERE id = ?";
        return jdbcTemplate.update(sql, newBalance, cardId);
    }

    public int updateCardStatus(Integer cardId, String status) {
        String sql = "UPDATE card_info SET status = ?, update_time = NOW() WHERE id = ?";
        return jdbcTemplate.update(sql, status, cardId);
    }

    public int addRechargeRecord(RechargeRecord record) {
        String sql = "INSERT INTO recharge_record (card_id, recharge_amount, operator, recharge_place, payment_method) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                record.getCardId(),
                record.getRechargeAmount(),
                record.getOperator(),
                record.getRechargePlace(),
                record.getPaymentMethod());
    }

    public int addConsumeRecord(ConsumeRecord record) {
        String sql = "INSERT INTO consume_record (card_id, consume_amount, operator, consume_place, merchant) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                record.getCardId(),
                record.getConsumeAmount(),
                record.getOperator(),
                record.getConsumePlace(),
                record.getMerchant());
    }
    
    public List<ConsumeRecord> getConsumeRecords(Integer cardId, Integer limit) {
        String sql = "SELECT * FROM consume_record WHERE card_id = ? ORDER BY create_time DESC LIMIT ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ConsumeRecord.class), cardId, limit);
    }
    
    public List<RechargeRecord> getRechargeRecords(Integer cardId, Integer limit) {
        String sql = "SELECT * FROM recharge_record WHERE card_id = ? ORDER BY create_time DESC LIMIT ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(RechargeRecord.class), cardId, limit);
    }

    public int addCardOperationLog(CardOperationLog log) {
        String sql = "INSERT INTO card_operation_log (card_id, operation_type, operator, operation_reason) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                log.getCardId(),
                log.getOperationType(),
                log.getOperator(),
                log.getOperationReason());
    }
    
    public int addOperationLog(CardOperationLog log) {
        return addCardOperationLog(log);
    }
    
    public int createCard(CardInfo card) {
        String sql = "INSERT INTO card_info (card_number, balance, status, registration_date, user_id) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                card.getCardNumber(),
                card.getBalance(),
                card.getStatus(),
                card.getRegistrationDate(),
                card.getUserId());
    }
    
    public int deleteCardByUserId(Integer userId) {
        String sql = "DELETE FROM card_info WHERE user_id = ?";
        return jdbcTemplate.update(sql, userId);
    }
}