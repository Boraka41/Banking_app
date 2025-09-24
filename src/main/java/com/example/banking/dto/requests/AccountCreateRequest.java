package com.example.banking.dto.requests;

import com.example.banking.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountCreateRequest {

    @NotBlank
    private String accountNumber;

    @NotNull
    private AccountType type;

    public @NotBlank String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(@NotBlank String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public @NotNull AccountType getType() {
        return type;
    }

    public void setType(@NotNull AccountType type) {
        this.type = type;
    }
}
