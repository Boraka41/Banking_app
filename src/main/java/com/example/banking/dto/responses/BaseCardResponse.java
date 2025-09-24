package com.example.banking.dto.responses;

import lombok.Data;

import java.time.YearMonth;

@Data
public abstract class BaseCardResponse {

    private Long id;
    private Long accountId;
    private String cardNumber;
    private YearMonth expiry;
    private String cvvLastDigits;
    private boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public YearMonth getExpiry() {
        return expiry;
    }

    public void setExpiry(YearMonth expiry) {
        this.expiry = expiry;
    }

    public String getCvvLastDigits() {
        return cvvLastDigits;
    }

    public void setCvvLastDigits(String cvvLastDigits) {
        this.cvvLastDigits = cvvLastDigits;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}