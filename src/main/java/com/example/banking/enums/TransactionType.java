package com.example.banking.enums;

public enum TransactionType {

    DEBT_PAYMENT("Borç Ödeme"),
    DEBT_CREATE("Borç Oluşma"),
    INVESTMENT_CREATE("Yatırım oluşturma");

    private final String displayName;

    TransactionType(String displayName) {

        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}

