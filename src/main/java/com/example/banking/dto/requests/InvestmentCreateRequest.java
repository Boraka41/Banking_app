package com.example.banking.dto.requests;

import com.example.banking.enums.Currency;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InvestmentCreateRequest {

    @NotNull
    private Currency currency;

    @NotNull
    private BigDecimal quantity;

    @NotNull
    private LocalDateTime date;

    public @NotNull Currency getCurrency() {
        return currency;
    }

    public void setCurrency(@NotNull Currency currency) {
        this.currency = currency;
    }

    public @NotNull BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(@NotNull BigDecimal quantity) {
        this.quantity = quantity;
    }

    public @NotNull LocalDateTime getDate() {
        return date;
    }

    public void setDate(@NotNull LocalDateTime date) {
        this.date = date;
    }
}
