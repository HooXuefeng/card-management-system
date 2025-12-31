package com.example.card.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RechargeRecord {
    private Integer id;
    private Integer cardId;
    private BigDecimal rechargeAmount;
    private LocalDateTime rechargeTime;
    private String operator;
    private String rechargePlace;
    private String paymentMethod;
    private LocalDateTime createTime;
}