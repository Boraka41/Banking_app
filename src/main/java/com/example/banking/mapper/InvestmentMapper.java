package com.example.banking.mapper;

import com.example.banking.dto.requests.InvestmentCreateRequest;
import com.example.banking.dto.responses.InvestmentResponse;
import com.example.banking.entity.Investment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(componentModel = "spring")
public interface InvestmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "unitPrice", ignore = true)
    Investment toEntity(InvestmentCreateRequest req);

    @Mapping(source = "account.id", target = "accountId")
    InvestmentResponse toResponseDto(Investment inv);
}


