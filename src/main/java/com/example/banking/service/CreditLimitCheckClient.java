package com.example.banking.service;

import com.example.banking.dto.shared.CreditCardLimitCheckRequest;
import com.example.banking.dto.shared.CreditCardLimitCheckResponse;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Service
public class CreditLimitCheckClient {

    private final KafkaTemplate<String, CreditCardLimitCheckRequest> kafkaTemplate;
    private final Map<String, CompletableFuture<CreditCardLimitCheckResponse>> pending = new ConcurrentHashMap<>();

    public CreditLimitCheckClient(KafkaTemplate<String, CreditCardLimitCheckRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CreditCardLimitCheckResponse checkLimit(Long userId, BigDecimal proposedBalance, Duration timeout) {
        String correlationId = UUID.randomUUID().toString();
        CreditCardLimitCheckRequest req = new CreditCardLimitCheckRequest();
        req.setCorrelationId(correlationId);
        req.setUserId(userId);
        req.setProposedNewCardBalance(proposedBalance);
        req.setReplyTo("credit-card.limit.check.response");

        CompletableFuture<CreditCardLimitCheckResponse> future = new CompletableFuture<>();
        pending.put(correlationId, future);

        kafkaTemplate.send("credit-card.limit.check.request", req);

        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            pending.remove(correlationId);
            throw new RuntimeException("Credit limit validation failed or timed out", e);
        }
    }

    @KafkaListener(topics = "credit-card.limit.check.response", groupId = "main-service-group")
    public void handleResponse(CreditCardLimitCheckResponse response) {
        CompletableFuture<CreditCardLimitCheckResponse> future = pending.remove(response.getCorrelationId());
        if (future != null) {
            future.complete(response);
        }
    }
}

