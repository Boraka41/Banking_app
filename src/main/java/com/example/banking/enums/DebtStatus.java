package com.example.banking.enums;

public enum DebtStatus {
    PENDING("Ödenecek"),
    PAID("Ödendi");

    private final String displayName;

    DebtStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

