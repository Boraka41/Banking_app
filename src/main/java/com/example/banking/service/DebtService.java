package com.example.banking.service;

import com.example.banking.dto.requests.DebtCreateRequest;
import com.example.banking.dto.requests.TransactionCreateRequest;
import com.example.banking.dto.responses.DebtResponse;
import com.example.banking.entity.Card;
import com.example.banking.entity.Debt;
import com.example.banking.entity.User;
import com.example.banking.enums.DebtStatus;
import com.example.banking.enums.TransactionType;
import com.example.banking.mapper.DebtMapper;
import com.example.banking.repository.CardRepository;
import com.example.banking.repository.DebtRepository;
import com.example.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class DebtService {

    private final DebtRepository debtRepository;
    private final CardRepository cardRepository;
    private final DebtMapper debtMapper;
    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @PreAuthorize("hasAnyRole('ROLE_banking_admin', 'ROLE_banking_user')")
    @Cacheable(value = "debts", key = "#cardId")
    public List<DebtResponse> getAllDebts(Long cardId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_banking_admin"));

        log.info("User [{}] requesting debts for cardId={} (isAdmin={})",
                auth.getName(), cardId, isAdmin);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Card not found, cardId={}", cardId);
                    return new EntityNotFoundException("Card not found: " + cardId);
                });

        if (!isAdmin) {
            Jwt jwt = (Jwt) auth.getPrincipal();
            String keycloakId = jwt.getClaim("sub");
            log.debug("Regular user with Keycloak ID={} checking ownership of card {}", keycloakId, cardId);

            User currentUser = userRepository.findByKeycloakId(keycloakId)
                    .orElseThrow(() -> {
                        log.error("User not found with Keycloak ID: {}", keycloakId);
                        return new UsernameNotFoundException("User not found with Keycloak ID: " + keycloakId);
                    });

            if (!card.getAccount().getUser().getId().equals(currentUser.getId())) {
                log.warn("Access denied: user {} is not owner of card {}", currentUser.getId(), cardId);
                throw new AccessDeniedException("You don't have permission to view these debts");
            }
        }

        List<DebtResponse> responses = card.getDebts().stream()
                .map(debtMapper::toResponseDto)
                .collect(Collectors.toList());

        log.info("Returning {} debts for cardId={} to user [{}]",
                responses.size(), cardId, auth.getName());
        return responses;
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    @Cacheable(value = "debt", key = "#debtId")
    public DebtResponse getOneDebt(Long debtId) {
        log.info("Retrieving debt by debtId={}", debtId);
        try {
            Debt debt = debtRepository.findById(debtId)
                    .orElseThrow(() -> new EntityNotFoundException("Debt not found: " + debtId));
            DebtResponse resp = debtMapper.toResponseDto(debt);
            log.debug("Debt details: {}", resp);
            return resp;
        } catch (EntityNotFoundException ex) {
            log.error("Debt not found, debtId={}", debtId, ex);
            throw ex;
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    @CachePut(value = {"debts", "debt"}, key = "#req.keyDebtId")
    public DebtResponse createDebt(Long cardId, DebtCreateRequest req) {
        log.info("Received request to create debt for cardId={}, payload={}", cardId, req);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Debt creation failed: card not found, cardId={}", cardId);
                    return new EntityNotFoundException("Card not found: " + cardId);
                });

        Debt debt = debtMapper.toEntity(req);
        debt.setAmount(req.getAmount());
        debt.setCard(card);
        debt.setCreatedAt(LocalDateTime.now());

        Debt saved = debtRepository.save(debt);
        log.info("Debt created successfully, debtId={}, amount={}", saved.getId(), saved.getAmount());

        transactionService.create(
                card.getAccount().getId(),
                TransactionCreateRequest.builder()
                        .type(TransactionType.DEBT_CREATE)
                        .amount(saved.getAmount())
                        .description("New debt added, debtId=" + saved.getId())
                        .counterpartyIban(null)
                        .build()
        );
        log.debug("Associated transaction created for debtId={}", saved.getId());

        return debtMapper.toResponseDto(saved);
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    @CacheEvict(value = {"debts", "debt"}, allEntries = true)
    public DebtResponse updateDebt(Long debtId, DebtCreateRequest req) {
        log.info("Received request to update debt debtId={}, payload={}", debtId, req);
        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> {
                    log.error("Debt not found for update, debtId={}", debtId);
                    return new EntityNotFoundException("Debt not found: " + debtId);
                });

        BigDecimal originalAmount = debt.getAmount();
        boolean paying = debt.getStatus() != req.getStatus()
                && req.getStatus() == DebtStatus.PAID;

        debt.setAmount(req.getAmount() != null ? req.getAmount() : debt.getAmount());
        debt.setDueDate(req.getDueDate());
        debt.setStatus(req.getStatus());
        debt.setDescription(req.getDescription());
        debt.setUpdatedAt(LocalDateTime.now());

        Debt updated = debtRepository.save(debt);
        log.info("Debt updated successfully, debtId={}, newStatus={}", updated.getId(), updated.getStatus());

        if (paying) {
            log.info("Debt payment detected, creating payment transaction for debtId={}", updated.getId());
            transactionService.create(
                    debt.getCard().getAccount().getId(),
                    TransactionCreateRequest.builder()
                            .type(TransactionType.DEBT_PAYMENT)
                            .amount(originalAmount)
                            .description("Debt paid, debtId=" + updated.getId())
                            .counterpartyIban(null)
                            .build()
            );
            log.debug("Payment transaction created for debtId={}", updated.getId());
        }

        return debtMapper.toResponseDto(updated);
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    @CacheEvict(value = {"debts", "debt"}, allEntries = true)
    public void deleteDebt(Long debtId) {
        log.info("Received request to delete debt, debtId={}", debtId);
        if (!debtRepository.existsById(debtId)) {
            log.error("Debt deletion failed: not found, debtId={}", debtId);
            throw new EntityNotFoundException("Debt not found: " + debtId);
        }
        debtRepository.deleteById(debtId);
        log.info("Debt deleted successfully, debtId={}", debtId);
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_user')")
    @CacheEvict(value = {"debts", "debt"}, allEntries = true)
    public DebtResponse payDebt(Long debtId) {
        log.info("Received request to pay debt, debtId={}", debtId);

        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> {
                    log.error("Debt not found, debtId={}", debtId);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Debt not found: " + debtId
                    );
                });

        if (debt.getStatus() == DebtStatus.PAID) {
            log.warn("Debt already paid, debtId={}", debtId);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Debt is already PAID: " + debtId
            );
        }

        Card card = debt.getCard();
        BigDecimal balance = card.getBalance();
        BigDecimal amount  = debt.getAmount();
        log.debug("Card balance={}, debt amount={}", balance, amount);

        if (balance.compareTo(amount) < 0) {
            log.error("Insufficient balance: needed={}, available={}", amount, balance);
            throw new ResponseStatusException(
                    HttpStatus.PAYMENT_REQUIRED,
                    "Insufficient balance to pay debt: " + debtId
            );
        }

        card.setBalance(balance.subtract(amount));
        cardRepository.save(card);
        log.info("Card balance updated after debt payment, cardId={}, newBalance={}",
                card.getId(), card.getBalance());

        debt.setStatus(DebtStatus.PAID);
        debt.setUpdatedAt(LocalDateTime.now());
        Debt paid = debtRepository.save(debt);
        log.info("Debt marked as PAID, debtId={}", paid.getId());

        transactionService.create(
                card.getAccount().getId(),
                TransactionCreateRequest.builder()
                        .type(TransactionType.DEBT_PAYMENT)
                        .amount(amount)
                        .description("Debt payment, debtId=" + paid.getId())
                        .counterpartyIban(null)
                        .build()
        );
        log.debug("Payment transaction created for debtId={}", paid.getId());

        return debtMapper.toResponseDto(paid);
    }

}
