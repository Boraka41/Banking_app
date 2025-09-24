package com.example.banking.controller;

import com.example.banking.dto.requests.AccountCreateRequest;
import com.example.banking.dto.responses.AccountResponse;
import com.example.banking.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<AccountResponse> all(Authentication authentication) {
        return accountService.listAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AccountResponse getOneAccount(@PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    @PostMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@PathVariable Long userId, @RequestBody @Valid AccountCreateRequest req) {
        return accountService.create(userId, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        accountService.delete(id);
    }
}
