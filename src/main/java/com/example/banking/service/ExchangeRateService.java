package com.example.banking.service;

import com.example.banking.client.ExchangeRateClient;
import com.example.banking.dto.responses.ExchangeRateResponse;
import com.example.banking.enums.Currency;
import org.springframework.stereotype.Service;

@Service
public class ExchangeRateService {

    private final ExchangeRateClient client;

    public ExchangeRateService(ExchangeRateClient client) {
        this.client = client;
    }

    public double getLatestRate(Currency base, Currency target) {
        ExchangeRateResponse resp = client.getLatestRates(base.name());
        Double rate = resp.getConversionRates().get(target.name());
        if (rate == null) {
            throw new IllegalArgumentException("Exchange rate not found for " + base + " → " + target);
        }
        return rate;
    }
}

