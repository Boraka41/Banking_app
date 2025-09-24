package com.example.banking.dto.shared;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditCardLimitCheckRequest {

    private String correlationId;
    private Long userId;
    private BigDecimal proposedNewCardBalance;
    private String replyTo;

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getProposedNewCardBalance() {
        return proposedNewCardBalance;
    }

    public void setProposedNewCardBalance(BigDecimal proposedNewCardBalance) {
        this.proposedNewCardBalance = proposedNewCardBalance;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }
}