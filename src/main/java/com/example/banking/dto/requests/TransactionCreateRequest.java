// src/main/java/com/example/banking/dto/requests/TransactionCreateRequest.java
package com.example.banking.dto.requests;

import com.example.banking.enums.Currency;
import com.example.banking.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TransactionCreateRequest {

    @NotNull
    private TransactionType type;

    private BigDecimal amount;

    private BigDecimal quantity;

    private String description;

    private String counterpartyIban;

    private Currency currency;

    public @NotNull TransactionType getType() {
        return type;
    }

    public void setType(@NotNull TransactionType type) {
        this.type = type;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCounterpartyIban() {
        return counterpartyIban;
    }

    public void setCounterpartyIban(String counterpartyIban) {
        this.counterpartyIban = counterpartyIban;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}
