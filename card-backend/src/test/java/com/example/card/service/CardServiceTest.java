package com.example.card.service;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.annotation.Transactional;

import com.example.card.dao.CardDao;
import com.example.card.entity.CardInfo;
import com.example.card.entity.ConsumeRecord;
import com.example.card.entity.RechargeRecord;
import com.example.card.exception.BusinessException;
import com.example.card.util.RedisUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CardServiceTest {

    @Mock
    private CardDao cardDao;

    @Mock
    private RedisUtil redisUtil;

    @InjectMocks
    private CardService cardService;

    private CardInfo testCard;

    @BeforeEach
    void setUp() {
        testCard = new CardInfo();
        testCard.setId(1);
        testCard.setUserId(100);
        testCard.setCardNumber("CARD001");
        testCard.setBalance(new BigDecimal("100.00"));
        testCard.setStatus("active");
        
        // 使用lenient模式处理stubbing
        lenient().when(redisUtil.tryLock(anyString(), anyLong())).thenReturn("requestId");
        lenient().doNothing().when(redisUtil).deleteCache(anyString());
        lenient().when(redisUtil.releaseLock(anyString(), anyString())).thenReturn(true);
        lenient().doNothing().when(redisUtil).setCache(anyString(), any(), anyLong());
    }

    @Test
    @Transactional
    void testRechargeSuccess() {
        // 模拟Redis锁获取成功
        when(redisUtil.tryLock(eq("lock:recharge:1"), eq(3L))).thenReturn("requestId");
        doNothing().when(redisUtil).deleteCache(anyString());
        when(redisUtil.releaseLock(eq("lock:recharge:1"), eq("requestId"))).thenReturn(true);
        
        // 模拟卡片存在且状态正常
        when(cardDao.getCardById(1)).thenReturn(testCard);
        
        // 模拟更新余额成功
        when(cardDao.updateCardBalance(eq(1), argThat(balance -> balance.compareTo(new BigDecimal("150.00")) == 0))).thenReturn(1);
        
        // 模拟添加充值记录成功
        when(cardDao.addRechargeRecord(any(RechargeRecord.class))).thenReturn(1);
        
        // 执行充值操作
        boolean result = cardService.recharge(1, new BigDecimal("50.00"), "testOperator", "testPlace", "testPayment");
        
        // 验证结果
        assertTrue(result);
        
        // 验证方法调用
        verify(redisUtil).tryLock("lock:recharge:1", 3L);
        verify(cardDao).getCardById(1);
        verify(cardDao).updateCardBalance(eq(1), argThat(balance -> balance.compareTo(new BigDecimal("150.00")) == 0));
        verify(cardDao).addRechargeRecord(any(RechargeRecord.class));
        verify(redisUtil).deleteCache("card:info:1");
        verify(redisUtil).deleteCache("card:user:100");
        verify(redisUtil).releaseLock("lock:recharge:1", "requestId");
    }

    @Test
    @Transactional
    void testRechargeCardNotFound() {
        // 模拟Redis锁获取成功
        when(redisUtil.tryLock(anyString(), anyInt())).thenReturn("requestId");
        when(redisUtil.releaseLock(anyString(), anyString())).thenReturn(true);
        
        // 模拟卡片不存在
        when(cardDao.getCardById(1)).thenReturn(null);
        
        // 执行充值操作并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            cardService.recharge(1, new BigDecimal("50.00"), "testOperator", "testPlace", "testPayment");
        });
        
        assertEquals(404, exception.getCode());
        assertEquals("卡片不存在", exception.getMessage());
        
        // 验证方法调用
        verify(redisUtil).tryLock("lock:recharge:1", 3);
        verify(cardDao).getCardById(1);
        verify(redisUtil).releaseLock("lock:recharge:1", "requestId");
        
        // 验证未调用更新方法
        verify(cardDao, never()).updateCardBalance(anyInt(), any(BigDecimal.class));
        verify(cardDao, never()).addRechargeRecord(any(RechargeRecord.class));
    }

    @Test
    @Transactional
    void testRechargeCardNotActive() {
        // 设置卡片状态为非活跃
        testCard.setStatus("frozen");
        
        // 模拟Redis锁获取成功
        when(redisUtil.tryLock(anyString(), anyInt())).thenReturn("requestId");
        when(redisUtil.releaseLock(anyString(), anyString())).thenReturn(true);
        
        // 模拟卡片存在但状态不正常
        when(cardDao.getCardById(1)).thenReturn(testCard);
        
        // 执行充值操作并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            cardService.recharge(1, new BigDecimal("50.00"), "testOperator", "testPlace", "testPayment");
        });
        
        assertEquals(400, exception.getCode());
        assertEquals("卡片状态异常，无法充值", exception.getMessage());
        
        // 验证方法调用
        verify(redisUtil).tryLock("lock:recharge:1", 3);
        verify(cardDao).getCardById(1);
        verify(redisUtil).releaseLock("lock:recharge:1", "requestId");
        
        // 验证未调用更新方法
        verify(cardDao, never()).updateCardBalance(anyInt(), any(BigDecimal.class));
        verify(cardDao, never()).addRechargeRecord(any(RechargeRecord.class));
    }

    @Test
    @Transactional
    void testRechargeSystemBusy() {
        // 重置Mock设置
        reset(redisUtil);
        
        // 模拟Redis锁获取失败（系统繁忙）
        when(redisUtil.tryLock(anyString(), anyLong())).thenReturn(null);
        
        // 执行充值操作并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            cardService.recharge(1, new BigDecimal("50.00"), "testOperator", "testPlace", "testPayment");
        });
        
        assertEquals(500, exception.getCode());
        assertEquals("系统繁忙，请稍后重试", exception.getMessage());
        
        // 验证方法调用
        verify(redisUtil).tryLock("lock:recharge:1", 3L);
        
        // 验证未调用其他方法
        verify(cardDao, never()).getCardById(anyInt());
        verify(cardDao, never()).updateCardBalance(anyInt(), any(BigDecimal.class));
        verify(cardDao, never()).addRechargeRecord(any(RechargeRecord.class));
        verify(redisUtil, never()).deleteCache(anyString());
        verify(redisUtil, never()).releaseLock(anyString(), anyString());
    }

    @Test
    @Transactional
    void testConsumeSuccess() {
        // 模拟Redis锁获取成功
        when(redisUtil.tryLock(eq("lock:consume:1"), eq(3L))).thenReturn("requestId");
        doNothing().when(redisUtil).deleteCache(anyString());
        when(redisUtil.releaseLock(eq("lock:consume:1"), eq("requestId"))).thenReturn(true);
        
        // 模拟卡片存在且状态正常
        when(cardDao.getCardById(1)).thenReturn(testCard);
        
        // 模拟更新余额成功
        when(cardDao.updateCardBalance(eq(1), argThat(balance -> balance.compareTo(new BigDecimal("70.00")) == 0))).thenReturn(1);
        
        // 模拟添加消费记录成功
        when(cardDao.addConsumeRecord(any(ConsumeRecord.class))).thenReturn(1);
        
        // 执行消费操作
        boolean result = cardService.consume(1, new BigDecimal("30.00"), "testOperator", "testPlace", "testMerchant");
        
        // 验证结果
        assertTrue(result);
        
        // 验证方法调用
        verify(redisUtil).tryLock("lock:consume:1", 3);
        verify(cardDao).getCardById(1);
        verify(cardDao).updateCardBalance(eq(1), argThat(balance -> balance.compareTo(new BigDecimal("70.00")) == 0));
        verify(cardDao).addConsumeRecord(any(ConsumeRecord.class));
        verify(redisUtil).deleteCache("card:info:1");
        verify(redisUtil).deleteCache("card:user:100");
        verify(redisUtil).releaseLock("lock:consume:1", "requestId");
    }

    @Test
    @Transactional
    void testConsumeInsufficientBalance() {
        // 模拟Redis锁获取成功
        when(redisUtil.tryLock(anyString(), anyInt())).thenReturn("requestId");
        when(redisUtil.releaseLock(anyString(), anyString())).thenReturn(true);
        
        // 模拟卡片存在但余额不足
        when(cardDao.getCardById(1)).thenReturn(testCard);
        
        // 执行消费操作并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            cardService.consume(1, new BigDecimal("150.00"), "testOperator", "testPlace", "testMerchant");
        });
        
        assertEquals(400, exception.getCode());
        assertEquals("余额不足", exception.getMessage());
        
        // 验证方法调用
        verify(redisUtil).tryLock("lock:consume:1", 3);
        verify(cardDao).getCardById(1);
        verify(redisUtil).releaseLock("lock:consume:1", "requestId");
        
        // 验证未调用更新方法
        verify(cardDao, never()).updateCardBalance(anyInt(), any(BigDecimal.class));
        verify(cardDao, never()).addConsumeRecord(any(ConsumeRecord.class));
    }

    @Test
    void testGetCardById() {
        // 模拟从缓存获取
        when(redisUtil.getCache("card:info:1")).thenReturn(testCard);
        
        // 执行查询
        CardInfo result = cardService.getCardById(1);
        
        // 验证结果
        assertEquals(testCard, result);
        
        // 验证方法调用
        verify(redisUtil).getCache("card:info:1");
        verify(cardDao, never()).getCardById(anyInt());
    }

    @Test
    void testGetCardByIdCacheMiss() {
        // 模拟缓存未命中
        when(redisUtil.getCache("card:info:1")).thenReturn(null);
        when(cardDao.getCardById(1)).thenReturn(testCard);
        doNothing().when(redisUtil).setCache(eq("card:info:1"), argThat(card -> card instanceof CardInfo && ((CardInfo) card).getId().equals(1)), eq(300));
        
        // 执行查询
        CardInfo result = cardService.getCardById(1);
        
        // 验证结果
        assertEquals(testCard, result);
        
        // 验证方法调用
        verify(redisUtil).getCache("card:info:1");
        verify(cardDao).getCardById(1);
        verify(redisUtil).setCache(eq("card:info:1"), argThat(card -> card instanceof CardInfo && ((CardInfo) card).getId().equals(1)), eq(300L));
    }
}