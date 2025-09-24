package com.example.banking.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRateResponse {

    private String result;

    @JsonProperty("base_code")
    private String baseCode;
    @JsonProperty("conversion_rates")
    private Map<String, Double> conversionRates;

}
