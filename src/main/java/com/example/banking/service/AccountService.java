package com.example.banking.service;

import com.example.banking.dto.requests.AccountCreateRequest;
import com.example.banking.dto.responses.AccountResponse;
import com.example.banking.entity.Account;
import com.example.banking.entity.User;
import com.example.banking.mapper.AccountMapper;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepo;
    private final UserRepository    userRepo;
    private final AccountMapper     accountMapper;
    private final UserRepository userRepository;


    @PreAuthorize("hasAnyRole('ROLE_banking_admin', 'ROLE_banking_user')")
    @Cacheable(value = "accounts", key = "#accountId")
    public List<AccountResponse> listAll() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_banking_admin"));

        try {
            if (isAdmin) {
                log.debug("Admin user accessing all accounts");
                List<Account> accounts = accountRepo.findAll();
                List<AccountResponse> response = accountMapper.toResponseList(accounts);
                log.info("Successfully retrieved {} accounts for admin user", accounts.size());
                return response;
            } else {
                Jwt jwt = (Jwt) auth.getPrincipal();
                String keycloakId = jwt.getClaim("sub");
                log.debug("Regular user accessing accounts with keycloak ID: {}", keycloakId);

                User user = userRepository.findByKeycloakId(keycloakId)
                        .orElseThrow(() -> {
                            log.error("User not found with Keycloak ID: {}", keycloakId);
                            return new UsernameNotFoundException("User not found with Keycloak ID: " + keycloakId);
                        });
                log.debug("Found user with ID: {}", user.getId());

                List<Account> userAccounts = accountRepo.findByUserId(user.getId());
                List<AccountResponse> response = accountMapper.toResponseList(userAccounts);
                log.info("Successfully retrieved {} accounts for user with ID: {}", userAccounts.size(), user.getId());
                return response;
            }
        } catch (Exception e) {
            log.error("Error occurred while retrieving accounts", e);
            throw e;
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    @Cacheable(value = "account", key = "#id")
    public AccountResponse getAccountById(Long id) {
        log.info("Retrieving account by id={}", id);
        try {
            AccountResponse resp = accountRepo.findById(id)
                    .map(accountMapper::toResponseDto)
                    .orElseThrow(() -> new EntityNotFoundException("Account not found: " + id));
            log.debug("Account details: {}", resp);
            return resp;
        } catch (EntityNotFoundException ex) {
            log.error("Account not found, id={}", id, ex);
            throw ex;
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    @CachePut(value = {"accounts", "account"}, key = "#req.accountNumber")
    public AccountResponse create(Long userId, AccountCreateRequest req) {
        log.info("Received request to create account for userId={}, payload={}", userId, req);
        Account account = accountMapper.toEntity(req);

        User user = userRepo.findById(userId)
                .orElseThrow(() -> {
                    log.error("Account creation failed: user not found, userId={}", userId);
                    return new EntityNotFoundException("User not found: " + userId);
                });
        account.setUser(user);

        Account saved = accountRepo.save(account);
        AccountResponse dto = accountMapper.toResponseDto(saved);
        log.info("Account successfully created, accountId={}", dto.getId());
        return dto;
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    @CacheEvict(value = {"accounts", "account"}, allEntries = true)
    public void delete(Long id) {
        log.info("Received request to delete account id={}", id);
        try {
            accountRepo.deleteById(id);
            log.info("Account deleted successfully, id={}", id);
        } catch (Exception ex) {
            log.error("Error occurred while deleting account, id={}", id, ex);
            throw ex;
        }
    }
}
