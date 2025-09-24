package com.example.banking.dto.requests;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreditCardCreateRequest extends BaseCardCreateRequest {

    @NotNull
    private BigDecimal balance;

    public @NotNull BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(@NotNull BigDecimal balance) {
        this.balance = balance;
    }

}
