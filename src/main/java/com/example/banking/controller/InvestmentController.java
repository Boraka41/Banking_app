package com.example.banking.controller;

import com.example.banking.dto.requests.InvestmentCreateRequest;
import com.example.banking.dto.responses.InvestmentResponse;
import com.example.banking.service.InvestmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class InvestmentController {

    private final InvestmentService service;

    public InvestmentController(InvestmentService service) {
        this.service = service;
    }

    @GetMapping("/accounts/{accountId}/investments")
    @ResponseStatus(HttpStatus.OK)
    public List<InvestmentResponse> getAll(@PathVariable Long accountId) {
        return service.getAllInvestments(accountId);
    }

    @GetMapping("/investments/{id}")
    @ResponseStatus(HttpStatus.OK)
    public InvestmentResponse getOne(@PathVariable Long id) {
        return service.getInvestment(id);
    }

    @PostMapping("/accounts/{accountId}/investments")
    @ResponseStatus(HttpStatus.CREATED)
    public InvestmentResponse create(@PathVariable Long accountId, @RequestBody @Valid InvestmentCreateRequest req) {
        return service.createInvestment(accountId, req);
    }

    @PutMapping("/investments/{id}")
    public InvestmentResponse update(@PathVariable Long id, @RequestBody @Valid InvestmentCreateRequest req) {
        return service.updateInvestment(id, req);
    }

    @DeleteMapping("/investments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deleteInvestment(id);
    }
}
