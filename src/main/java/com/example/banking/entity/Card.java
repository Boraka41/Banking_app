package com.example.banking.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "card_type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "cards")
public abstract class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, length = 19)
    private String cardNumber;

    @Column(nullable = false, columnDefinition="VARCHAR(7)")
    private YearMonth expiry;

    @Column(name = "cvv_last_digits", length = 2)
    private String cvvLastDigits;

    @Column(nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Debt> debts = new ArrayList<>();

    public abstract BigDecimal getBalance();

    public abstract void setBalance(BigDecimal balance);

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Debt> getDebts() {
        return debts;
    }

    public void setDebts(List<Debt> debts) {
        this.debts = debts;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public YearMonth getExpiry() {
        return expiry;
    }

    public void setExpiry(YearMonth expiry) {
        this.expiry = expiry;
    }

    public String getCvvLastDigits() {
        return cvvLastDigits;
    }

    public void setCvvLastDigits(String cvvLastDigits) {
        this.cvvLastDigits = cvvLastDigits;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}

