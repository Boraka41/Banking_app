package com.example.banking.client;

import com.example.banking.dto.responses.ExchangeRateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
public class ExchangeRateClient {

    private final RestTemplate rest;
    private final String apiKey;
    private final String baseUrl;

    public ExchangeRateClient(RestTemplate rest,
                              @Value("${exchangeRateApi.key}") String apiKey,
                              @Value("${exchangeRateApi.url}") String baseUrl) {
        this.rest = rest;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public ExchangeRateResponse getLatestRates(String baseCode) {
        String url = String.format("%s/%s/latest/%s", baseUrl, apiKey, baseCode);
        return rest.getForObject(url, ExchangeRateResponse.class);
    }
}