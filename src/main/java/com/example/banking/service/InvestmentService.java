package com.example.banking.service;

import com.example.banking.dto.requests.InvestmentCreateRequest;
import com.example.banking.dto.responses.InvestmentResponse;
import com.example.banking.dto.requests.TransactionCreateRequest;
import com.example.banking.entity.Card;
import com.example.banking.entity.Account;
import com.example.banking.entity.Investment;
import com.example.banking.entity.User;
import com.example.banking.enums.Currency;
import com.example.banking.enums.TransactionType;
import com.example.banking.mapper.InvestmentMapper;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.CardRepository;
import com.example.banking.repository.InvestmentRepository;
import com.example.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.banking.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentRepository repo;
    private final AccountRepository accountRepo;
    private final InvestmentMapper mapper;
    private final ExchangeRateService rateService;
    private final TransactionService txService;
    private final CardRepository cardRepo;
    private final UserRepository userRepository;


    @PreAuthorize("hasAnyRole('ROLE_banking_admin', 'ROLE_banking_user')")
    @Cacheable(value = "investments", key = "#accountId")
    public List<InvestmentResponse> getAllInvestments(Long accountId) {
        log.info("Listing all investments for accountId={}", accountId);

        try {
            Account account = accountRepo.findById(accountId)
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

            List<InvestmentResponse> list = account.getInvestments().stream()
                    .map(mapper::toResponseDto)
                    .collect(Collectors.toList());

            log.info("Successfully retrieved {} investments for account {}", list.size(), accountId);
            return list;

        } catch (ResourceNotFoundException | UsernameNotFoundException e) {
            log.error("Resource not found error: {}", e.getMessage());
            throw e;
        } catch (AccessDeniedException e) {
            log.error("Access denied error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while retrieving investments for account {}: {}", accountId, e.getMessage(), e);
            throw new RuntimeException("Error retrieving investments", e);
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    @Cacheable(value = "investment", key = "#id")
    public InvestmentResponse getInvestment(Long id) {
        log.info("Retrieving investment by id={}", id);
        Investment inv = repo.findById(id)
                .orElseThrow(() -> {
                    log.error("Investment not found, id={}", id);
                    return new RuntimeException("Investment not found: " + id);
                });
        InvestmentResponse resp = mapper.toResponseDto(inv);
        log.debug("Investment details: {}", resp);
        return resp;
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    @CachePut(value = {"investments", "investment"}, key = "req.id")//düzenle
    public InvestmentResponse createInvestment(Long accountId, InvestmentCreateRequest req) {
        log.info("Received request to create investment for accountId={}, payload={}", accountId, req);

        Account acct = accountRepo.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Investment creation failed: account not found, accountId={}", accountId);
                    return new RuntimeException("Account not found: " + accountId);
                });

        Card card = cardRepo.findByAccount_Id(accountId)
                .stream()
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Investment creation failed: no card for accountId={}", accountId);
                    return new RuntimeException("Card not found for account: " + accountId);
                });
        log.debug("Using cardId={} with balance={}", card.getId(), card.getBalance());

        double rate = rateService.getLatestRate(req.getCurrency(), Currency.TRY);
        BigDecimal investmentAmount = req.getQuantity().multiply(BigDecimal.valueOf(rate));

        if (card.getBalance().compareTo(investmentAmount) < 0) {
            log.error("Insufficient balance: needed={}, available={}", investmentAmount, card.getBalance());
            throw new RuntimeException("Insufficient balance for investment.");
        }

        card.setBalance(card.getBalance().subtract(investmentAmount));
        cardRepo.save(card);
        log.debug("Card balance updated, new balance={}", card.getBalance());

        Investment inv = mapper.toEntity(req);
        inv.setAccount(acct);
        inv.setUnitPrice(investmentAmount);

        Investment saved = repo.save(inv);
        log.info("Investment created successfully, investmentId={}, amountInvested={}", saved.getId(), investmentAmount);

        txService.create(
                accountId,
                TransactionCreateRequest.builder()
                        .type(TransactionType.INVESTMENT_CREATE)
                        .amount(investmentAmount)
                        .description("Investment created, id=" + saved.getId())
                        .counterpartyIban(null)
                        .build()
        );
        log.debug("Associated investment transaction recorded for investmentId={}", saved.getId());

        return mapper.toResponseDto(saved);
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    @CacheEvict(value = {"investments", "investment"}, allEntries = true)
    public InvestmentResponse updateInvestment(Long id, InvestmentCreateRequest req) {
        log.info("Received request to update investment id={}, payload={}", id, req);
        Investment inv = repo.findById(id)
                .orElseThrow(() -> {
                    log.error("Investment not found for update, id={}", id);
                    return new RuntimeException("Investment not found: " + id);
                });

        if (inv.getCurrency() != req.getCurrency()) {
            double rate = rateService.getLatestRate(req.getCurrency(), Currency.TRY);
            log.debug("Currency change detected. Old={}, New={}, Rate={}", inv.getCurrency(), req.getCurrency(), rate);
            inv.setUnitPrice(BigDecimal.valueOf(rate));
            inv.setCurrency(req.getCurrency());
        }

        inv.setQuantity(req.getQuantity());
        inv.setDate(req.getDate());
        Investment saved = repo.save(inv);
        log.info("Investment updated successfully, id={}", saved.getId());
        return mapper.toResponseDto(saved);
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    @CacheEvict(value = {"investments", "investment"}, allEntries = true)
    public void deleteInvestment(Long id) {
        log.info("Received request to delete investment, id={}", id);
        repo.deleteById(id);
        log.info("Investment deleted successfully, id={}", id);
    }
}
