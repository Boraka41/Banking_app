package com.example.banking.dto.shared;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditSummaryDto {
    private Long salary;
    private BigDecimal existingCreditCardBalances;

    public BigDecimal getExistingCreditCardBalances() {
        return existingCreditCardBalances;
    }

    public void setExistingCreditCardBalances(BigDecimal existingCreditCardBalances) {
        this.existingCreditCardBalances = existingCreditCardBalances;
    }

    public Long getSalary() {
        return salary;
    }

    public void setSalary(Long salary) {
        this.salary = salary;
    }
}