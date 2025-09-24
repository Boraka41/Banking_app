package com.example.banking.controller;

import com.example.banking.dto.requests.TransactionCreateRequest;
import com.example.banking.dto.responses.TransactionResponse;
import com.example.banking.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TransactionController {

    private final TransactionService txService;

    public TransactionController(TransactionService txService) {
        this.txService = txService;
    }

    @GetMapping("/accounts/{accountId}/transactions")
    @ResponseStatus(HttpStatus.OK)
    public List<TransactionResponse> list(@PathVariable Long accountId) {
        return txService.getAll(accountId);
    }

    @GetMapping("/transactions/{txId}")
    @ResponseStatus(HttpStatus.OK)
    public TransactionResponse getOne(@PathVariable Long txId) {
        return txService.getOne(txId);
    }

    @PostMapping("/accounts/{accountId}/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@PathVariable Long accountId, @RequestBody @Valid TransactionCreateRequest req) {
        return txService.create(accountId, req);
    }
}
