package com.example.card.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ConsumeRecord {
    private Integer id;
    private Integer cardId;
    private BigDecimal consumeAmount;
    private LocalDateTime consumeTime;
    private String operator;
    private String consumePlace;
    private String merchantName;
    private String merchant;
    private LocalDateTime createTime;
    
    // 为了兼容性，添加一个方法将merchant映射到merchantName
    public String getMerchant() {
        return merchant != null ? merchant : merchantName;
    }
    
    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }
}