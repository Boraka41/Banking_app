package com.example.banking.controller;

import com.example.banking.dto.requests.DebtCreateRequest;
import com.example.banking.dto.responses.DebtResponse;
import com.example.banking.service.DebtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DebtController {

    private final DebtService debtService;

    public DebtController(DebtService debtService) {
        this.debtService = debtService;
    }

    @GetMapping("/cards/{cardId}/debts")
    @ResponseStatus(HttpStatus.OK)
    public List<DebtResponse> getAllDebts(@PathVariable Long cardId) {
        return debtService.getAllDebts(cardId);
    }

    @GetMapping("/debts/{debtId}")
    @ResponseStatus(HttpStatus.OK)
    public DebtResponse getOneDebt(@PathVariable Long debtId) {
        return debtService.getOneDebt(debtId);
    }

    @PostMapping("/cards/{cardId}/debts")
    @ResponseStatus(HttpStatus.CREATED)
    public DebtResponse createDebt(@PathVariable Long cardId, @RequestBody @Valid DebtCreateRequest req) {
        return debtService.createDebt(cardId, req);
    }

    @PutMapping("/debts/{debtId}")
    public DebtResponse updateDebt(@PathVariable Long debtId, @RequestBody @Valid DebtCreateRequest req) {
        return debtService.updateDebt(debtId, req);
    }

    @DeleteMapping("/debts/{debtId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDebt(@PathVariable Long debtId) {
        debtService.deleteDebt(debtId);
    }

    @PutMapping("/cards/{cardId}/debts/{debtId}/pay")
    public DebtResponse payDebt(@PathVariable Long debtId) {
        return debtService.payDebt(debtId);
    }

}
