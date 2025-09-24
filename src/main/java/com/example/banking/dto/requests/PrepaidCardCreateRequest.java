package com.example.banking.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PrepaidCardCreateRequest extends BaseCardCreateRequest {

    @NotNull
    private BigDecimal balance;

    public @NotNull BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(@NotNull BigDecimal balance) {
        this.balance = balance;
    }
}
