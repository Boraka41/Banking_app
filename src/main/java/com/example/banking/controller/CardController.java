package com.example.banking.controller;

import com.example.banking.dto.requests.CreditCardCreateRequest;
import com.example.banking.dto.requests.DebitCardCreateRequest;
import com.example.banking.dto.requests.PrepaidCardCreateRequest;
import com.example.banking.dto.requests.VirtualCardCreateRequest;
import com.example.banking.dto.responses.*;
import com.example.banking.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<BaseCardResponse> getAllCards(){
        return cardService.getAllCards();
    }

    @GetMapping("/{cardId}")
    @ResponseStatus(HttpStatus.OK)
    public BaseCardResponse getOneCard(@PathVariable Long cardId){
        return cardService.getOneCard(cardId);
    }

    @PostMapping("/accounts/{accountId}/credit")
    @ResponseStatus(HttpStatus.CREATED)
    public CreditCardResponse createCreditCard(@PathVariable Long accountId, @RequestBody  @Valid CreditCardCreateRequest req){
        return cardService.createCreditCard(accountId, req);
    }

    @PutMapping("/cards/credit/{cardId}")
    public CreditCardResponse updateCreditCard(@PathVariable Long cardId, @RequestBody @Valid CreditCardCreateRequest req){
        return cardService.updateCreditCard(cardId, req);
    }

    @PostMapping("/accounts/{accountId}/virtual")
    @ResponseStatus(HttpStatus.CREATED)
    public VirtualCardResponse createVirtualCard(@PathVariable Long accountId, @RequestBody @Valid VirtualCardCreateRequest req) {
        return cardService.createVirtualCard(accountId, req);
    }

    @PutMapping("/cards/virtual/{cardId}")
    public VirtualCardResponse updateVirtualCard(@PathVariable Long cardId, @RequestBody @Valid VirtualCardCreateRequest req) {
        return cardService.updateVirtualCard(cardId, req);
    }

    @PostMapping("/accounts/{accountId}/prepaid")
    @ResponseStatus(HttpStatus.CREATED)
    public PrepaidCardResponse createPrepaidCard(@PathVariable Long accountId, @RequestBody @Valid PrepaidCardCreateRequest req) {
        return cardService.createPrepaidCard(accountId, req);
    }

    @PutMapping("/cards/prepaid/{cardId}")
    public PrepaidCardResponse updatePrepaidCard(@PathVariable Long cardId, @RequestBody @Valid PrepaidCardCreateRequest req) {
        return cardService.updatePrepaidCard(cardId, req);
    }

    @PostMapping("/accounts/{accountId}/debit")
    @ResponseStatus(HttpStatus.CREATED)
    public DebitCardResponse createDebitCard(@PathVariable Long accountId, @RequestBody @Valid DebitCardCreateRequest req) {
        return cardService.createDebitCard(accountId, req);
    }

    @PutMapping("/cards/debit/{cardId}")
    public DebitCardResponse updateDebitCard(@PathVariable Long cardId, @RequestBody @Valid DebitCardCreateRequest req) {
        return cardService.updateDebitCard(cardId, req);
    }

    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOneCard(@PathVariable Long cardId){
        cardService.deleteOneCard(cardId);
    }
}
