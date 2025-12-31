package com.example.card.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CardInfo {
    private Integer id;
    private String cardNumber;
    private BigDecimal balance;
    private String status;
    private LocalDate registrationDate;
    private Integer userId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}