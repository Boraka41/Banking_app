package com.example.banking.mapper;

import com.example.banking.dto.requests.DebtCreateRequest;
import com.example.banking.dto.responses.DebtResponse;
import com.example.banking.entity.Debt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DebtMapper {

    @Mapping(source = "card.id", target = "cardId")
    DebtResponse toResponseDto(Debt debt);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "keyDebtId", source = "keyDebtId")
    Debt toEntity(DebtCreateRequest req);
}
