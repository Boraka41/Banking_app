package com.example.banking.enums;

public enum Currency{
    TRY("Turkish Lira", "₺"),
    USD("US Dollar", "$"),
    EUR("Euro", "€");

    private final String displayName;
    private final String symbol;

    Currency(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return name() + " (" + symbol + ")";
    }
}
