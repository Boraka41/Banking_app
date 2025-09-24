package com.example.banking.dto.responses;

import com.example.banking.enums.AccountType;
import lombok.Data;

@Data
public class AccountResponse {

    private Long id;
    private String accountNumber;
    private AccountType type;
    private Long userId;

    public String getAccountNumber() {return accountNumber;}

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }
}
