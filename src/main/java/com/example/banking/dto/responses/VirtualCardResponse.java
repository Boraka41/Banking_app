package com.example.banking.dto.responses;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VirtualCardResponse extends BaseCardResponse{

    private BigDecimal balance;

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
