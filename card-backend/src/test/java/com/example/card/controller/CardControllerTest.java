package com.example.card.controller;

import com.example.card.service.CardService;
import com.example.card.service.UserService;
import com.example.card.entity.CardInfo;
import com.example.card.entity.UserInfo;
import com.example.card.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@WebMvcTest(CardController.class)
@Import(SecurityConfig.class)
public class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private CardInfo testCard;

    @BeforeEach
    void setUp() {
        testCard = new CardInfo();
        testCard.setId(1);
        testCard.setUserId(100);
        testCard.setCardNumber("CARD001");
        testCard.setBalance(new BigDecimal("100.00"));
        testCard.setStatus("active");
    }

    @Test
    void testGetCardInfo() throws Exception {
        // 模拟服务返回
        when(cardService.getCardById(1)).thenReturn(testCard);

        // 执行请求
        mockMvc.perform(get("/card/info/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("查询成功"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.cardNumber").value("CARD001"))
                .andExpect(jsonPath("$.data.balance").value(100.00))
                .andExpect(jsonPath("$.data.status").value("active"));

        // 验证服务调用
        verify(cardService).getCardById(1);
    }

    @Test
    void testRechargeSuccess() throws Exception {
        // 模拟服务返回
        when(cardService.recharge(eq(1), eq(new BigDecimal("50.00")), eq("testOperator"), eq("testPlace"), eq("testPayment")))
                .thenReturn(true);

        // 执行请求
        mockMvc.perform(post("/card/recharge")
                        .param("cardId", "1")
                        .param("amount", "50.00")
                        .param("operator", "testOperator")
                        .param("place", "testPlace")
                        .param("paymentMethod", "testPayment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("充值成功"));

        // 验证服务调用
        verify(cardService).recharge(eq(1), eq(new BigDecimal("50.00")), eq("testOperator"), eq("testPlace"), eq("testPayment"));
    }

    @Test
    void testRechargeInvalidAmount() throws Exception {
        // 执行请求（金额为负数）
        mockMvc.perform(post("/card/recharge")
                        .param("cardId", "1")
                        .param("amount", "-50.00")
                        .param("operator", "testOperator")
                        .param("place", "testPlace")
                        .param("paymentMethod", "testPayment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("充值金额必须大于0"));

        // 验证服务未被调用
        verify(cardService, never()).recharge(anyInt(), any(BigDecimal.class), anyString(), anyString(), anyString());
    }

    @Test
    void testRechargeExceedLimit() throws Exception {
        // 执行请求（金额超过限制）
        mockMvc.perform(post("/card/recharge")
                        .param("cardId", "1")
                        .param("amount", "50000.00")
                        .param("operator", "testOperator")
                        .param("place", "testPlace")
                        .param("paymentMethod", "testPayment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("单次充值金额不能超过10000元"));

        // 验证服务未被调用
        verify(cardService, never()).recharge(anyInt(), any(BigDecimal.class), anyString(), anyString(), anyString());
    }

    @Test
    void testConsumeSuccess() throws Exception {
        // 模拟服务返回
        when(cardService.getCardById(1)).thenReturn(testCard);
        when(cardService.consume(eq(1), eq(new BigDecimal("30.00")), eq("testOperator"), eq("testPlace"), eq("testMerchant")))
                .thenReturn(true);

        // 执行请求
        mockMvc.perform(post("/card/consume")
                        .param("cardId", "1")
                        .param("amount", "30.00")
                        .param("operator", "testOperator")
                        .param("place", "testPlace")
                        .param("merchant", "testMerchant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("消费成功"));

        // 验证服务调用
        verify(cardService).getCardById(1);
        verify(cardService).consume(eq(1), eq(new BigDecimal("30.00")), eq("testOperator"), eq("testPlace"), eq("testMerchant"));
    }

    @Test
    void testConsumeInvalidAmount() throws Exception {
        // 执行请求（金额为负数）
        mockMvc.perform(post("/card/consume")
                        .param("cardId", "1")
                        .param("amount", "-30.00")
                        .param("operator", "testOperator")
                        .param("place", "testPlace")
                        .param("merchant", "testMerchant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("消费金额必须大于0"));

        // 验证服务未被调用
        verify(cardService, never()).getCardById(anyInt());
        verify(cardService, never()).consume(anyInt(), any(BigDecimal.class), anyString(), anyString(), anyString());
    }

    @Test
    void testFreezeCardSuccess() throws Exception {
        // 模拟服务返回
        when(cardService.freezeCard(eq(1), eq("testReason"), eq("testOperator")))
                .thenReturn(true);

        // 执行请求
        mockMvc.perform(post("/card/freeze")
                        .param("cardId", "1")
                        .param("operator", "testOperator")
                        .param("remark", "testReason"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("卡片冻结成功"));

        // 验证服务调用
        verify(cardService).freezeCard(eq(1), eq("testReason"), eq("testOperator"));
    }

    @Test
    void testUnfreezeCardSuccess() throws Exception {
        // 模拟服务返回
        when(cardService.unfreezeCard(eq(1), eq("testReason"), eq("testOperator")))
                .thenReturn(true);

        // 执行请求
        mockMvc.perform(post("/card/unfreeze")
                        .param("cardId", "1")
                        .param("operator", "testOperator")
                        .param("remark", "testReason"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("卡片解冻成功"));

        // 验证服务调用
        verify(cardService).unfreezeCard(eq(1), eq("testReason"), eq("testOperator"));
    }

    @Test
    void testFreezeCardStatusWithoutAuth() throws Exception {
        // 执行请求（不带Authorization头）
        mockMvc.perform(post("/card/status/freeze"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("系统错误: Required request header 'Authorization' for method parameter type String is not present"));

        // 验证服务未被调用
        verify(cardService, never()).freezeCard(anyInt(), anyString(), anyString());
    }

    @Test
    void testFreezeCardStatusWithAuth() throws Exception {
        // 模拟用户服务返回
        UserInfo testUser = new UserInfo();
        testUser.setUserId(100);
        testUser.setUserName("testUser");
        
        when(userService.getUserIdByToken("testToken")).thenReturn(100);
        when(cardService.getCardByUserId(100)).thenReturn(testCard);
        when(cardService.freezeCard(eq(1), eq("用户自助挂失"), eq("用户")))
                .thenReturn(true);

        // 执行请求
        mockMvc.perform(post("/card/status/freeze")
                        .header("Authorization", "Bearer testToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("冻结成功"));

        // 验证服务调用
        verify(userService).getUserIdByToken("testToken");
        verify(cardService).getCardByUserId(100);
        verify(cardService).freezeCard(eq(1), eq("用户自助挂失"), eq("用户"));
    }
}