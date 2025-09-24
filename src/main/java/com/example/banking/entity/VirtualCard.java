package com.example.banking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@DiscriminatorValue("VIRTUAL")
public class VirtualCard extends Card{

    @Column(precision = 19, scale = 2)
    private BigDecimal balance;

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
