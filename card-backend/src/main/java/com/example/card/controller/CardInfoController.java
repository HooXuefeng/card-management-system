package com.example.card.controller;

import com.example.card.entity.CardInfo;
import com.example.card.entity.ConsumeRecord;
import com.example.card.entity.RechargeRecord;
import com.example.card.service.CardService;
import com.example.card.service.UserService;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class CardInfoController {
    @Resource
    private CardService cardService;
    @Resource
    private UserService userService;
    @Resource
    private DataSource dataSource;

    @GetMapping("/info")
    public Map<String, Object> getCardInfo(@RequestHeader("Authorization") String authorization) {
        Map<String, Object> result = new HashMap<>();

        if (authorization == null || authorization.isEmpty()) {
            result.put("code", 401);
            result.put("msg", "请先登录");
            return result;
        }

        try {
            // 从token中获取用户信息
            String token = authorization.replace("Bearer ", "");
            Integer userId = userService.getUserIdByToken(token);
            
            // 添加调试信息
            System.out.println("Debug - Token: " + token);
            System.out.println("Debug - UserId: " + userId);
            
            if (userId == null) {
                result.put("code", 401);
                result.put("msg", "token无效，请重新登录");
                return result;
            }

            // 根据用户ID获取卡片信息
            CardInfo cardInfo = cardService.getCardByUserId(userId);
            
            // 添加调试信息
            System.out.println("Debug - CardInfo: " + (cardInfo != null ? cardInfo.toString() : "null"));
            
            if (cardInfo == null) {
                result.put("code", 404);
                result.put("msg", "未找到关联的饭卡");
                return result;
            }

            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", cardInfo);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "系统错误：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/consume/records")
    public Map<String, Object> getConsumeRecords(@RequestHeader("Authorization") String authorization) {
        Map<String, Object> result = new HashMap<>();

        if (authorization == null || authorization.isEmpty()) {
            result.put("code", 401);
            result.put("msg", "请先登录");
            return result;
        }

        try {
            String token = authorization.replace("Bearer ", "");
            Integer userId = userService.getUserIdByToken(token);
            
            if (userId == null) {
                result.put("code", 401);
                result.put("msg", "token无效，请重新登录");
                return result;
            }

            CardInfo cardInfo = cardService.getCardByUserId(userId);
            if (cardInfo == null) {
                result.put("code", 404);
                result.put("msg", "未找到关联的饭卡");
                return result;
            }

            List<ConsumeRecord> records = cardService.getConsumeRecords(cardInfo.getId(), 10);
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", records);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "系统错误：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/recharge/records")
    public Map<String, Object> getRechargeRecords(@RequestHeader("Authorization") String authorization) {
        Map<String, Object> result = new HashMap<>();

        if (authorization == null || authorization.isEmpty()) {
            result.put("code", 401);
            result.put("msg", "请先登录");
            return result;
        }

        try {
            String token = authorization.replace("Bearer ", "");
            Integer userId = userService.getUserIdByToken(token);
            
            if (userId == null) {
                result.put("code", 401);
                result.put("msg", "token无效，请重新登录");
                return result;
            }

            CardInfo cardInfo = cardService.getCardByUserId(userId);
            if (cardInfo == null) {
                result.put("code", 404);
                result.put("msg", "未找到关联的饭卡");
                return result;
            }

            List<RechargeRecord> records = cardService.getRechargeRecords(cardInfo.getId(), 10);
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", records);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "系统错误：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/recharge")
    public Map<String, Object> recharge(
            @RequestHeader("Authorization") String authorization,
            @RequestParam java.math.BigDecimal amount) {
        Map<String, Object> result = new HashMap<>();

        if (authorization == null || authorization.isEmpty()) {
            result.put("code", 401);
            result.put("msg", "请先登录");
            return result;
        }

        try {
            // 数据校验
            if (amount == null || amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                result.put("code", 400);
                result.put("msg", "充值金额必须大于0");
                return result;
            }
            
            if (amount.compareTo(new java.math.BigDecimal("10000")) > 0) {
                result.put("code", 400);
                result.put("msg", "单次充值金额不能超过10000元");
                return result;
            }
            
            String token = authorization.replace("Bearer ", "");
            Integer userId = userService.getUserIdByToken(token);
            
            if (userId == null) {
                result.put("code", 401);
                result.put("msg", "token无效，请重新登录");
                return result;
            }

            CardInfo cardInfo = cardService.getCardByUserId(userId);
            if (cardInfo == null) {
                result.put("code", 404);
                result.put("msg", "未找到关联的饭卡");
                return result;
            }
            
            // 检查卡片状态
            if (!"active".equals(cardInfo.getStatus())) {
                result.put("code", 400);
                result.put("msg", "卡片状态异常，无法充值");
                return result;
            }

            boolean success = cardService.recharge(cardInfo.getId(), amount, "用户", "线上充值", "支付宝");
            if (success) {
                result.put("code", 200);
                result.put("msg", "充值成功");
            } else {
                result.put("code", 500);
                result.put("msg", "充值失败，请稍后重试");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "系统错误：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/status/change")
    public Map<String, Object> changeCardStatus(
            @RequestHeader("Authorization") String authorization) {
        Map<String, Object> result = new HashMap<>();

        if (authorization == null || authorization.isEmpty()) {
            result.put("code", 401);
            result.put("msg", "请先登录");
            return result;
        }

        try {
            String token = authorization.replace("Bearer ", "");
            Integer userId = userService.getUserIdByToken(token);
            
            if (userId == null) {
                result.put("code", 401);
                result.put("msg", "token无效，请重新登录");
                return result;
            }

            CardInfo cardInfo = cardService.getCardByUserId(userId);
            if (cardInfo == null) {
                result.put("code", 404);
                result.put("msg", "未找到关联的饭卡");
                return result;
            }

            boolean success;
            if ("active".equals(cardInfo.getStatus())) {
                success = cardService.freezeCard(cardInfo.getId(), "用户", "用户自助挂失");
            } else {
                success = cardService.unfreezeCard(cardInfo.getId(), "用户", "用户自助解冻");
            }

            if (success) {
                result.put("code", 200);
                result.put("msg", "操作成功");
            } else {
                result.put("code", 500);
                result.put("msg", "操作失败，请稍后重试");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "系统错误：" + e.getMessage());
        }
        return result;
    }
    
    @PostMapping("/create")
    public Map<String, Object> createCard(@RequestHeader("Authorization") String authorization) {
        Map<String, Object> result = new HashMap<>();

        if (authorization == null || authorization.isEmpty()) {
            result.put("code", 401);
            result.put("msg", "请先登录");
            return result;
        }

        try {
            String token = authorization.replace("Bearer ", "");
            Integer userId = userService.getUserIdByToken(token);
            
            if (userId == null) {
                result.put("code", 401);
                result.put("msg", "token无效，请重新登录");
                return result;
            }

            // 检查用户是否已有卡片
            CardInfo existingCard = cardService.getCardByUserId(userId);
            if (existingCard != null) {
                // 如果用户已有卡片，删除旧卡片再创建新卡片
                cardService.deleteCardByUserId(userId);
            }

            // 创建新卡片
            CardInfo newCard = new CardInfo();
            newCard.setUserId(userId);
            newCard.setBalance(java.math.BigDecimal.ZERO);
            newCard.setStatus("active");
            newCard.setRegistrationDate(java.time.LocalDate.now());

            boolean success = cardService.createCard(newCard);
            if (success) {
                result.put("code", 200);
                result.put("msg", "饭卡创建成功");
            } else {
                result.put("code", 500);
                result.put("msg", "饭卡创建失败，请稍后重试");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "系统错误：" + e.getMessage());
        }
        return result;
    }
    
    @GetMapping("/test-db")
    public Map<String, Object> testDatabaseConnection() {
        Map<String, Object> result = new HashMap<>();
        try {
            Connection connection = dataSource.getConnection();
            if (connection != null) {
                result.put("code", 200);
                result.put("msg", "数据库连接成功");
                result.put("connection", connection.isValid(5));
                result.put("database", connection.getCatalog());
                result.put("username", connection.getMetaData().getUserName());
                connection.close();
            } else {
                result.put("code", 500);
                result.put("msg", "无法获取数据库连接");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "数据库连接失败：" + e.getMessage());
            result.put("error_class", e.getClass().getName());
            // 获取更详细的错误信息
            if (e.getCause() != null) {
                result.put("cause", e.getCause().getMessage());
            }
        }
        return result;
    }
}