package com.example.card.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CardOperationLog {
    private Integer id;
    private Integer cardId;
    private String operationType;
    private LocalDateTime operationTime;
    private String operator;
    private String remark;
    private String operationReason;
    
    // 为了兼容性，添加一个方法将remark映射到operationReason
    public String getOperationReason() {
        return operationReason != null ? operationReason : remark;
    }
    
    public void setOperationReason(String operationReason) {
        this.operationReason = operationReason;
    }
}