package com.example.banking.service;

import com.example.banking.dto.requests.CreditCardCreateRequest;
import com.example.banking.dto.requests.DebitCardCreateRequest;
import com.example.banking.dto.requests.PrepaidCardCreateRequest;
import com.example.banking.dto.requests.VirtualCardCreateRequest;
import com.example.banking.dto.responses.*;
import com.example.banking.dto.shared.CreditCardLimitCheckResponse;
import com.example.banking.entity.*;
import com.example.banking.mapper.CardMapper;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CardService {

    private final CardRepository    cardRepo;
    private final AccountRepository accountRepo;
    private final CardMapper        cardMapper;
    private final CreditLimitCheckClient creditLimitCheckClient;

    @PreAuthorize("hasAnyRole('banking_admin', 'banking_user')")
    @Cacheable(value = "cards", key = "'all_' + #root.target.getCurrentUserKeycloakId()")
    public List<BaseCardResponse> getAllCards() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserKeycloakId = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_banking_admin"));

        List<Card> cards;
        if (isAdmin) {
            log.debug("Admin user accessing all cards");
            cards = cardRepo.findAll();
        } else {
            log.debug("Regular user accessing their cards. User KeycloakId: {}", currentUserKeycloakId);
            cards = cardRepo.findByAccount_User_KeycloakId(currentUserKeycloakId);
        }

        return getAllCardResponses(cards);
    }


    private List<BaseCardResponse> getAllCardResponses(List<Card> cards) {
        List<BaseCardResponse> responses = new ArrayList<>();

        Map<Class<?>, List<Card>> cardsByType = cards.stream()
                .collect(Collectors.groupingBy(Card::getClass));

        if (cardsByType.containsKey(CreditCard.class)) {
            List<CreditCard> creditCards = castList(cardsByType.get(CreditCard.class), CreditCard.class);
            responses.addAll(cardMapper.toCreditCardResponseList(creditCards));
        }

        if (cardsByType.containsKey(DebitCard.class)) {
            List<DebitCard> debitCards = castList(cardsByType.get(DebitCard.class), DebitCard.class);
            responses.addAll(cardMapper.toDebitCardResponseList(debitCards));
        }

        if (cardsByType.containsKey(VirtualCard.class)) {
            List<VirtualCard> virtualCards = castList(cardsByType.get(VirtualCard.class), VirtualCard.class);
            responses.addAll(cardMapper.toVirtualCardResponseList(virtualCards));
        }

        if (cardsByType.containsKey(PrepaidCard.class)) {
            List<PrepaidCard> prepaidCards = castList(cardsByType.get(PrepaidCard.class), PrepaidCard.class);
            responses.addAll(cardMapper.toPrepaidCardResponseList(prepaidCards));
        }

        return responses;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> castList(List<?> list, Class<T> clazz) {
        return list.stream()
                .map(item -> (T) item)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('banking_admin', 'banking_user')")
    @Cacheable(value = "cards", key = "#cardId")
    public BaseCardResponse getOneCard(Long cardId) {
        log.info("Retrieving card by cardId={}", cardId);
        try {
            BaseCardResponse resp = cardRepo.findById(cardId)
                    .map(this::mapToResponse)
                    .orElseThrow(() -> new EntityNotFoundException("Card not found: " + cardId));
            log.debug("Card details: {}", resp);
            return resp;
        } catch (EntityNotFoundException ex) {
            log.error("Card not found, cardId={}", cardId, ex);
            throw ex;
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    @CachePut(value = {"cards", "card"}, key = "#req.cardNumber")
    public CreditCardResponse createCreditCard(Long accountId, CreditCardCreateRequest req) {
        log.info("Received request to create credit card for accountId={}, payload={}", accountId, req);
        var accountOpt = accountRepo.findById(accountId);
        if (accountOpt.isEmpty()) {
            log.error("Credit card creation failed: account not found, accountId={}", accountId);
            throw new EntityNotFoundException("Account not found: " + accountId);
        }

        Account account = accountOpt.get();
        User user = account.getUser();

        CreditCard card = cardMapper.toCreditCard(req);
        card.setAccount(account);

        BigDecimal proposedBalance = ((CreditCard) card).getBalance();

        CreditCardLimitCheckResponse validationResponse = creditLimitCheckClient.checkLimit(
                user.getId(), proposedBalance, Duration.ofSeconds(5)
        );

        if (!validationResponse.isApproved()) {
            log.warn("Credit card creation rejected by validation service: {}", validationResponse.getReason());
            throw new IllegalStateException("Credit card limit rule violated: " + validationResponse.getReason());
        }

        CreditCard saved = cardRepo.save(card);
        CreditCardResponse response = cardMapper.toCreditCardResponse(saved);
        log.info("Credit card successfully created, cardId={}", response.getId());
        return response;
    }


    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    @CachePut(value = {"cards", "card"}, key = "#req.cardNumber")
    public DebitCardResponse createDebitCard(Long accountId, DebitCardCreateRequest req) {
        log.info("Received request to create debit card for accountId={}, payload={}", accountId, req);
        var accountOpt = accountRepo.findById(accountId);
        if (accountOpt.isEmpty()) {
            log.error("Debit card creation failed: account not found, accountId={}", accountId);
            throw new EntityNotFoundException("Account not found: " + accountId);
        }
        DebitCard card = cardMapper.toDebitCard(req);
        card.setAccount(accountOpt.get());

        DebitCard saved = cardRepo.save(card);
        DebitCardResponse response = cardMapper.toDebitCardResponse(saved);
        log.info("Debit card successfully created, cardId={}", response.getId());
        return response;
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    @CachePut(value = {"cards", "card"}, key = "#req.cardNumber")
    public VirtualCardResponse createVirtualCard(Long accountId, VirtualCardCreateRequest req) {
        log.info("Received request to create virtual card for accountId={}, payload={}", accountId, req);
        var accountOpt = accountRepo.findById(accountId);
        if (accountOpt.isEmpty()) {
            log.error("Virtual card creation failed: account not found, accountId={}", accountId);
            throw new EntityNotFoundException("Account not found: " + accountId);
        }
        VirtualCard card = cardMapper.toVirtualCard(req);
        card.setAccount(accountOpt.get());

        VirtualCard saved = cardRepo.save(card);
        VirtualCardResponse response = cardMapper.toVirtualCardResponse(saved);
        log.info("Virtual card successfully created, cardId={}", response.getId());
        return response;
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    @CachePut(value = {"cards", "card"}, key = "#req.cardNumber")
    public PrepaidCardResponse createPrepaidCard(Long accountId, PrepaidCardCreateRequest req) {
        log.info("Received request to create prepaid card for accountId={}, payload={}", accountId, req);
        var accountOpt = accountRepo.findById(accountId);
        if (accountOpt.isEmpty()) {
            log.error("Prepaid card creation failed: account not found, accountId={}", accountId);
            throw new EntityNotFoundException("Account not found: " + accountId);
        }
        PrepaidCard card = cardMapper.toPrepaidCard(req);
        card.setAccount(accountOpt.get());

        PrepaidCard saved = cardRepo.save(card);
        PrepaidCardResponse response = cardMapper.toPrepaidCardResponse(saved);
        log.info("Prepaid card successfully created, cardId={}", response.getId());
        return response;
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    public CreditCardResponse updateCreditCard(Long cardId, CreditCardCreateRequest req) {
        log.info("Received request to update credit card cardId={}, payload={}", cardId, req);
        var existingOpt = cardRepo.findById(cardId)
                .filter(c -> c instanceof CreditCard)
                .map(CreditCard.class::cast);

        CreditCard existing = existingOpt
                .orElseThrow(() -> {
                    log.error("Credit card update failed: not found, cardId={}", cardId);
                    return new EntityNotFoundException("CreditCard not found: " + cardId);
                });

        CreditCard updated = cardMapper.toCreditCard(req);
        updated.setId(existing.getId());
        updated.setAccount(existing.getAccount());

        CreditCard saved = cardRepo.save(updated);
        CreditCardResponse response = cardMapper.toCreditCardResponse(saved);
        log.info("Credit card successfully updated, cardId={}", response.getId());
        return response;
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    public DebitCardResponse updateDebitCard(Long cardId, DebitCardCreateRequest req) {
        log.info("Received request to update debit card cardId={}, payload={}", cardId, req);
        var existingOpt = cardRepo.findById(cardId)
                .filter(c -> c instanceof DebitCard)
                .map(DebitCard.class::cast);

        DebitCard existing = existingOpt
                .orElseThrow(() -> {
                    log.error("Debit card update failed: not found, cardId={}", cardId);
                    return new EntityNotFoundException("DebitCard not found: " + cardId);
                });

        DebitCard updated = cardMapper.toDebitCard(req);
        updated.setId(existing.getId());
        updated.setAccount(existing.getAccount());

        DebitCard saved = cardRepo.save(updated);
        DebitCardResponse response = cardMapper.toDebitCardResponse(saved);
        log.info("Debit card successfully updated, cardId={}", response.getId());
        return response;
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    public VirtualCardResponse updateVirtualCard(Long cardId, VirtualCardCreateRequest req) {
        log.info("Received request to update virtual card cardId={}, payload={}", cardId, req);
        var existingOpt = cardRepo.findById(cardId)
                .filter(c -> c instanceof VirtualCard)
                .map(VirtualCard.class::cast);

        VirtualCard existing = existingOpt
                .orElseThrow(() -> {
                    log.error("Virtual card update failed: not found, cardId={}", cardId);
                    return new EntityNotFoundException("VirtualCard not found: " + cardId);
                });

        VirtualCard updated = cardMapper.toVirtualCard(req);
        updated.setId(existing.getId());
        updated.setAccount(existing.getAccount());

        VirtualCard saved = cardRepo.save(updated);
        VirtualCardResponse response = cardMapper.toVirtualCardResponse(saved);
        log.info("Virtual card successfully updated, cardId={}", response.getId());
        return response;
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    public PrepaidCardResponse updatePrepaidCard(Long cardId, PrepaidCardCreateRequest req) {
        log.info("Received request to update prepaid card cardId={}, payload={}", cardId, req);
        var existingOpt = cardRepo.findById(cardId)
                .filter(c -> c instanceof PrepaidCard)
                .map(PrepaidCard.class::cast);

        PrepaidCard existing = existingOpt
                .orElseThrow(() -> {
                    log.error("Prepaid card update failed: not found, cardId={}", cardId);
                    return new EntityNotFoundException("PrepaidCard not found: " + cardId);
                });

        PrepaidCard updated = cardMapper.toPrepaidCard(req);
        updated.setId(existing.getId());
        updated.setAccount(existing.getAccount());

        PrepaidCard saved = cardRepo.save(updated);
        PrepaidCardResponse response = cardMapper.toPrepaidCardResponse(saved);
        log.info("Prepaid card successfully updated, cardId={}", response.getId());
        return response;
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    public void deleteOneCard(Long cardId) {
        log.info("Received request to delete card cardId={}", cardId);
        try {
            cardRepo.deleteById(cardId);
            log.info("Card deleted successfully, cardId={}", cardId);
        } catch (Exception ex) {
            log.error("Error occurred while deleting card, cardId={}", cardId, ex);
            throw ex;
        }
    }

    private BaseCardResponse mapToResponse(Card card) {
        if (card instanceof CreditCard cc) {
            return cardMapper.toCreditCardResponse(cc);
        } else if (card instanceof DebitCard dc) {
            return cardMapper.toDebitCardResponse(dc);
        } else if (card instanceof VirtualCard vc) {
            return cardMapper.toVirtualCardResponse(vc);
        } else if (card instanceof PrepaidCard pc) {
            return cardMapper.toPrepaidCardResponse(pc);
        } else {
            log.error("Unknown card type encountered, cardId={}", card.getId());
            throw new EntityNotFoundException("Unknown card type: " + card.getId());
        }
    }
}
