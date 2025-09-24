package com.example.banking.dto.requests;

import com.example.banking.enums.DebtStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DebtCreateRequest {

    @NotNull
    private BigDecimal amount;

    @NotNull
    private LocalDate dueDate;

    @NotNull
    private DebtStatus status;

    private String description;

    @NotNull
    private Long keyDebtId;

    public @NotNull BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(@NotNull BigDecimal amount) {
        this.amount = amount;
    }

    public @NotNull DebtStatus getStatus() {
        return status;
    }

    public void setStatus(@NotNull DebtStatus status) {
        this.status = status;
    }

    public @NotNull LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(@NotNull LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getDescription() {
        return description;
    }

    public @NotNull Long getKeyDebtId() {
        return keyDebtId;
    }

    public void setKeyDebtId(@NotNull Long keyDebtId) {
        this.keyDebtId = keyDebtId;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
