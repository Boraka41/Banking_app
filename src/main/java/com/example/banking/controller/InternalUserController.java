package com.example.banking.controller;

import com.example.banking.dto.shared.CreditSummaryDto;
import com.example.banking.entity.CreditCard;
import com.example.banking.entity.User;
import com.example.banking.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Objects;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserRepository userRepo;

    public InternalUserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/{userId}/credit-summary")
    public CreditSummaryDto getCreditSummary(@PathVariable Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        BigDecimal existing = sumExistingCreditCardBalances(user);
        CreditSummaryDto dto = new CreditSummaryDto();
        dto.setSalary(user.getSalary());
        dto.setExistingCreditCardBalances(existing);
        return dto;
    }

    private BigDecimal sumExistingCreditCardBalances(User user) {
        return user.getAccounts().stream()
                .flatMap(acc -> acc.getCards().stream())
                .filter(c -> c instanceof CreditCard)
                .map(c -> ((CreditCard) c).getBalance())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

