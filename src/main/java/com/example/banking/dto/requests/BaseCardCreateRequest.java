package com.example.banking.dto.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.YearMonth;

@Data
public abstract class BaseCardCreateRequest {

    private Long accountId;
    private String cardNumber;
    private YearMonth expiry;
    private String cvvLastDigits;
    private boolean active;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}