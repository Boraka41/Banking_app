package com.example.banking.service;

import com.example.banking.dto.requests.TransactionCreateRequest;
import com.example.banking.dto.responses.TransactionResponse;
import com.example.banking.entity.Account;
import com.example.banking.entity.Transaction;
import com.example.banking.entity.User;
import com.example.banking.enums.Currency;
import com.example.banking.exception.ResourceNotFoundException;
import com.example.banking.mapper.TransactionMapper;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository tRepo;
    private final TransactionMapper     tMapper;
    private final AccountRepository     accountRepo;
    private final ExchangeRateService   rateService;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @PreAuthorize("hasAnyRole('ROLE_banking_admin', 'ROLE_banking_user')")
    @Cacheable(value = "transactions", key = "#accountId")
    public List<TransactionResponse> getAll(Long accountId) {
        log.info("Listing all transactions for accountId={}", accountId);

        try {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> {
                        log.error("Account not found with ID: {}", accountId);
                        return new ResourceNotFoundException("Account not found with ID: " + accountId);
                    });

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_banking_admin"));

            if (!isAdmin) {
                Jwt jwt = (Jwt) auth.getPrincipal();
                String keycloakId = jwt.getClaim("sub");
                log.debug("Checking access permission for user with keycloak ID: {} to account: {}", keycloakId, accountId);

                User currentUser = userRepository.findByKeycloakId(keycloakId)
                        .orElseThrow(() -> {
                            log.error("User not found with Keycloak ID: {}", keycloakId);
                            return new UsernameNotFoundException("User not found with Keycloak ID: " + keycloakId);
                        });

                if (!account.getUser().getId().equals(currentUser.getId())) {
                    log.error("User {} attempted unauthorized access to account {}", currentUser.getId(), accountId);
                    throw new AccessDeniedException("You don't have permission to access this account");
                }
            }

            List<TransactionResponse> list = tRepo.findByAccountId(accountId)
                    .stream()
                    .map(tMapper::toResponseDto)
                    .collect(Collectors.toList());

            log.info("Successfully retrieved {} transactions for account {}", list.size(), accountId);
            return list;

        } catch (ResourceNotFoundException | UsernameNotFoundException e) {
            log.error("Resource not found error: {}", e.getMessage());
            throw e;
        } catch (AccessDeniedException e) {
            log.error("Access denied error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while retrieving transactions for account {}: {}", accountId, e.getMessage(), e);
            throw new RuntimeException("Error retrieving transactions", e);
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    @Cacheable(value = "transactions", key = "#txId")
    public TransactionResponse getOne(Long txId) {
        log.info("Retrieving transaction by txId={}", txId);
        Transaction tx = tRepo.findById(txId)
                .orElseThrow(() -> {
                    log.error("Transaction not found, txId={}", txId);
                    return new EntityNotFoundException("Transaction not found: " + txId);
                });
        TransactionResponse resp = tMapper.toResponseDto(tx);
        log.debug("Transaction details: {}", resp);
        return resp;
    }

    @CacheEvict(value = "transactions", allEntries = true)
    public TransactionResponse create(Long accountId, TransactionCreateRequest req) {
        log.info("Received request to create transaction for accountId={}, payload={}", accountId, req);
        var acct = accountRepo.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Transaction creation failed: account not found, accountId={}", accountId);
                    return new EntityNotFoundException("Account not found: " + accountId);
                });

        var tx = tMapper.toEntity(req);
        tx.setAccount(acct);

        BigDecimal finalAmount;
        if (req.getQuantity() != null && req.getCurrency() != null) {
            double rate = rateService.getLatestRate(req.getCurrency(), Currency.TRY);
            log.debug("Exchange rate used: {}", rate);
            finalAmount = req.getQuantity().multiply(BigDecimal.valueOf(rate));
        } else if (req.getAmount() != null) {
            finalAmount = req.getAmount();
        } else {
            log.error("Transaction creation failed: neither amount nor quantity+currency provided");
            throw new IllegalArgumentException("Either amount or (quantity & currency) must be provided");
        }

        tx.setAmount(finalAmount);
        tx.setType(req.getType());
        tx.setDescription(req.getDescription());
        tx.setCounterpartyIban(req.getCounterpartyIban());
        tx.setTimestamp(LocalDateTime.now());

        Transaction saved = tRepo.save(tx);
        log.info("Transaction created successfully, txId={}, amount={}", saved.getId(), saved.getAmount());
        return tMapper.toResponseDto(saved);
    }
}
