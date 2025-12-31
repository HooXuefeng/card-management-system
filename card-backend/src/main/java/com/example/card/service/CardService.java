package com.example.card.service;

import com.example.card.dao.CardDao;
import com.example.card.entity.CardInfo;
import com.example.card.entity.CardOperationLog;
import com.example.card.entity.ConsumeRecord;
import com.example.card.entity.RechargeRecord;
import com.example.card.exception.BusinessException;
import com.example.card.util.RedisUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CardService {
    @Resource
    private CardDao cardDao;
    @Resource
    private RedisUtil redisUtil;

    public CardInfo getCardById(Integer cardId) {
        String cacheKey = "card:info:" + cardId;
        CardInfo card = (CardInfo) redisUtil.getCache(cacheKey);
        if (card == null) {
            card = cardDao.getCardById(cardId);
            if (card != null) {
                redisUtil.setCache(cacheKey, card, 300);
            }
        }
        return card;
    }

    public CardInfo getCardByUserId(Integer userId) {
        System.out.println("Debug - CardService.getCardByUserId called with userId: " + userId);
        String cacheKey = "card:user:" + userId;
        CardInfo card = (CardInfo) redisUtil.getCache(cacheKey);
        System.out.println("Debug - Card from cache: " + (card != null ? card.toString() : "null"));
        if (card == null) {
            System.out.println("Debug - Cache miss, querying database");
            card = cardDao.getCardByUserId(userId);
            System.out.println("Debug - Card from database: " + (card != null ? card.toString() : "null"));
            if (card != null) {
                redisUtil.setCache(cacheKey, card, 300);
            }
        }
        return card;
    }

    public List<ConsumeRecord> getConsumeRecords(Integer cardId, Integer limit) {
        return cardDao.getConsumeRecords(cardId, limit);
    }

    public List<RechargeRecord> getRechargeRecords(Integer cardId, Integer limit) {
        return cardDao.getRechargeRecords(cardId, limit);
    }

    @Transactional
    public boolean recharge(Integer cardId, BigDecimal amount, String operator, String place, String paymentMethod) {
        String lockKey = "lock:recharge:" + cardId;
        String requestId = redisUtil.tryLock(lockKey, 3);

        if (requestId == null) {
            throw new BusinessException("系统繁忙，请稍后重试");
        }

        try {
            CardInfo card = cardDao.getCardById(cardId);
            if (card == null) {
                throw new BusinessException(404, "卡片不存在");
            }
            
            if (!"active".equals(card.getStatus())) {
                throw new BusinessException(400, "卡片状态异常，无法充值");
            }

            BigDecimal newBalance = card.getBalance().add(amount);
            cardDao.updateCardBalance(cardId, newBalance);

            RechargeRecord record = new RechargeRecord();
            record.setCardId(cardId);
            record.setRechargeAmount(amount);
            record.setOperator(operator);
            record.setRechargePlace(place);
            record.setPaymentMethod(paymentMethod);
            cardDao.addRechargeRecord(record);

            redisUtil.deleteCache("card:info:" + cardId);
            redisUtil.deleteCache("card:user:" + card.getUserId());
            return true;
        } finally {
            redisUtil.releaseLock(lockKey, requestId);
        }
    }

    @Transactional
    public boolean consume(Integer cardId, BigDecimal amount, String operator, String place, String merchant) {
        String lockKey = "lock:consume:" + cardId;
        String requestId = redisUtil.tryLock(lockKey, 3);

        if (requestId == null) {
            throw new BusinessException("系统繁忙，请稍后重试");
        }

        try {
            CardInfo card = cardDao.getCardById(cardId);
            if (card == null) {
                throw new BusinessException(404, "卡片不存在");
            }
            
            if (!"active".equals(card.getStatus())) {
                throw new BusinessException(400, "卡片状态异常，无法消费");
            }

            if (card.getBalance().compareTo(amount) < 0) {
                throw new BusinessException(400, "余额不足");
            }

            BigDecimal newBalance = card.getBalance().subtract(amount);
            cardDao.updateCardBalance(cardId, newBalance);

            ConsumeRecord record = new ConsumeRecord();
            record.setCardId(cardId);
            record.setConsumeAmount(amount);
            record.setOperator(operator);
            record.setConsumePlace(place);
            record.setMerchantName(merchant);
            cardDao.addConsumeRecord(record);

            redisUtil.deleteCache("card:info:" + cardId);
            redisUtil.deleteCache("card:user:" + card.getUserId());
            return true;
        } finally {
            redisUtil.releaseLock(lockKey, requestId);
        }
    }

    @Transactional
    public boolean freezeCard(Integer cardId, String reason, String operator) {
        CardInfo card = cardDao.getCardById(cardId);
        if (card == null) {
            throw new BusinessException(404, "卡片不存在");
        }
        
        if (!"active".equals(card.getStatus())) {
            throw new BusinessException(400, "卡片状态异常，无法冻结");
        }
        
        boolean success = cardDao.updateCardStatus(cardId, "frozen") > 0;
        if (success) {
            CardOperationLog log = new CardOperationLog();
            log.setCardId(cardId);
            log.setOperationType("freeze");
            log.setOperator(operator);
            log.setOperationReason(reason);
            cardDao.addOperationLog(log);
            
            redisUtil.deleteCache("card:info:" + cardId);
            redisUtil.deleteCache("card:user:" + card.getUserId());
        }
        return success;
    }

    @Transactional
    public boolean unfreezeCard(Integer cardId, String reason, String operator) {
        CardInfo card = cardDao.getCardById(cardId);
        if (card == null) {
            throw new BusinessException(404, "卡片不存在");
        }
        
        if (!"frozen".equals(card.getStatus())) {
            throw new BusinessException(400, "卡片状态异常，无法解冻");
        }
        
        boolean success = cardDao.updateCardStatus(cardId, "active") > 0;
        if (success) {
            CardOperationLog log = new CardOperationLog();
            log.setCardId(cardId);
            log.setOperationType("unfreeze");
            log.setOperator(operator);
            log.setOperationReason(reason);
            cardDao.addOperationLog(log);
            
            redisUtil.deleteCache("card:info:" + cardId);
            redisUtil.deleteCache("card:user:" + card.getUserId());
        }
        return success;
    }
    
    @Transactional
    public boolean createCard(CardInfo card) {
        // 设置默认值
        if (card.getBalance() == null) {
            card.setBalance(new BigDecimal("0"));
        }
        if (card.getStatus() == null) {
            card.setStatus("active");
        }
        if (card.getRegistrationDate() == null) {
            card.setRegistrationDate(java.time.LocalDate.now());
        }
        
        // 生成卡号
        if (card.getCardNumber() == null || card.getCardNumber().isEmpty()) {
            card.setCardNumber(generateCardNumber());
        }
        
        int result = cardDao.createCard(card);
        if (result > 0) {
            // 清除缓存
            redisUtil.deleteCache("card:user:" + card.getUserId());
            return true;
        }
        return false;
    }
    
    @Transactional
    public boolean deleteCardByUserId(Integer userId) {
        int result = cardDao.deleteCardByUserId(userId);
        if (result > 0) {
            // 清除缓存
            redisUtil.deleteCache("card:user:" + userId);
            return true;
        }
        return false;
    }
    
    private String generateCardNumber() {
        // 生成10位随机卡号，以"1"开头
        StringBuilder cardNumber = new StringBuilder("1");
        for (int i = 0; i < 9; i++) {
            cardNumber.append((int)(Math.random() * 10));
        }
        return cardNumber.toString();
    }
}