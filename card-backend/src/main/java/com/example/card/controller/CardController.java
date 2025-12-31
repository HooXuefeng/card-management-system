package com.example.card.controller;

import com.example.card.entity.CardInfo;
import com.example.card.service.CardService;
import com.example.card.service.UserService;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/card")
public class CardController {
    @Resource
    private CardService cardService;
    @Resource
    private UserService userService;

    @GetMapping("/{cardId}")
    public Map<String, Object> getCard(@PathVariable Integer cardId) {
        Map<String, Object> result = new HashMap<>();
        CardInfo card = cardService.getCardById(cardId);
        if (card != null) {
            result.put("code", 200);
            result.put("data", card);
        } else {
            result.put("code", 404);
            result.put("msg", "卡片不存在");
        }
        return result;
    }

    @GetMapping("/info/{cardId}")
    public Map<String, Object> getCardInfo(@PathVariable Integer cardId) {
        Map<String, Object> result = new HashMap<>();
        try {
            CardInfo card = cardService.getCardById(cardId);
            if (card != null) {
                result.put("code", 200);
                result.put("msg", "查询成功");
                result.put("data", card);
            } else {
                result.put("code", 404);
                result.put("msg", "卡片不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "系统错误：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/recharge")
    public Map<String, Object> recharge(
            @RequestParam Integer cardId,
            @RequestParam BigDecimal amount,
            @RequestParam String operator,
            @RequestParam String place,
            @RequestParam String paymentMethod) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 数据校验
            if (cardId == null || cardId <= 0) {
                result.put("code", 400);
                result.put("msg", "无效的卡片ID");
                return result;
            }
            
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
            
            if (operator == null || operator.trim().isEmpty()) {
                result.put("code", 400);
                result.put("msg", "操作员信息不能为空");
                return result;
            }
            
            if (place == null || place.trim().isEmpty()) {
                result.put("code", 400);
                result.put("msg", "充值地点不能为空");
                return result;
            }
            
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                result.put("code", 400);
                result.put("msg", "支付方式不能为空");
                return result;
            }
            
            boolean success = cardService.recharge(cardId, amount, operator, place, paymentMethod);
            if (success) {
                result.put("code", 200);
                result.put("msg", "充值成功");
            } else {
                result.put("code", 500);
                result.put("msg", "充值失败（卡片异常/并发冲突）");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "系统错误：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/consume")
    public Map<String, Object> consume(
            @RequestParam Integer cardId,
            @RequestParam BigDecimal amount,
            @RequestParam String operator,
            @RequestParam String place,
            @RequestParam String merchant) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 数据校验
            if (cardId == null || cardId <= 0) {
                result.put("code", 400);
                result.put("msg", "无效的卡片ID");
                return result;
            }
            
            if (amount == null || amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                result.put("code", 400);
                result.put("msg", "消费金额必须大于0");
                return result;
            }
            
            if (amount.compareTo(new java.math.BigDecimal("5000")) > 0) {
                result.put("code", 400);
                result.put("msg", "单次消费金额不能超过5000元");
                return result;
            }
            
            if (operator == null || operator.trim().isEmpty()) {
                result.put("code", 400);
                result.put("msg", "操作员信息不能为空");
                return result;
            }
            
            if (place == null || place.trim().isEmpty()) {
                result.put("code", 400);
                result.put("msg", "消费地点不能为空");
                return result;
            }
            
            if (merchant == null || merchant.trim().isEmpty()) {
                result.put("code", 400);
                result.put("msg", "商户名称不能为空");
                return result;
            }
            
            // 检查卡片状态和余额
            CardInfo cardInfo = cardService.getCardById(cardId);
            if (cardInfo == null) {
                result.put("code", 404);
                result.put("msg", "卡片不存在");
                return result;
            }
            
            if (!"active".equals(cardInfo.getStatus())) {
                result.put("code", 400);
                result.put("msg", "卡片状态异常，无法消费");
                return result;
            }
            
            if (cardInfo.getBalance().compareTo(amount) < 0) {
                result.put("code", 400);
                result.put("msg", "余额不足，无法消费");
                return result;
            }
            
            boolean success = cardService.consume(cardId, amount, operator, place, merchant);
            if (success) {
                result.put("code", 200);
                result.put("msg", "消费成功");
            } else {
                result.put("code", 500);
                result.put("msg", "消费失败（并发冲突）");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "系统错误：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/freeze")
    public Map<String, Object> freezeCard(
            @RequestParam Integer cardId,
            @RequestParam String operator,
            @RequestParam String remark) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = cardService.freezeCard(cardId, remark, operator);
            if (success) {
                result.put("code", 200);
                result.put("msg", "卡片冻结成功");
            } else {
                result.put("code", 500);
                result.put("msg", "卡片冻结失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "系统错误：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/status/freeze")
    public Map<String, Object> freezeCardStatus(
            @RequestHeader("Authorization") String authorization) {
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

            boolean success = cardService.freezeCard(cardInfo.getId(), "用户自助挂失", "用户");
            if (success) {
                result.put("code", 200);
                result.put("msg", "冻结成功");
            } else {
                result.put("code", 500);
                result.put("msg", "冻结失败，请稍后重试");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "系统错误：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/unfreeze")
    public Map<String, Object> unfreezeCard(
            @RequestParam Integer cardId,
            @RequestParam String operator,
            @RequestParam String remark) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = cardService.unfreezeCard(cardId, remark, operator);
            if (success) {
                result.put("code", 200);
                result.put("msg", "卡片解冻成功");
            } else {
                result.put("code", 500);
                result.put("msg", "卡片解冻失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "系统错误：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/status/unfreeze")
    public Map<String, Object> unfreezeCardStatus(
            @RequestHeader("Authorization") String authorization) {
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

            boolean success = cardService.unfreezeCard(cardInfo.getId(), "用户自助解冻", "用户");
            if (success) {
                result.put("code", 200);
                result.put("msg", "解冻成功");
            } else {
                result.put("code", 500);
                result.put("msg", "解冻失败，请稍后重试");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "系统错误：" + e.getMessage());
        }
        return result;
    }
}