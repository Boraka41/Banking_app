package com.example.banking.mapper;

import com.example.banking.dto.requests.TransactionCreateRequest;
import com.example.banking.dto.responses.TransactionResponse;
import com.example.banking.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", imports = LocalDateTime.class)
public interface TransactionMapper {

    @Mapping(source = "account.id", target = "accountId")
    TransactionResponse toResponseDto(Transaction tx);

    @Mapping(source = "amount",            target = "amount")
    @Mapping(source = "type",              target = "type")
    @Mapping(source = "description",       target = "description")
    @Mapping(source = "counterpartyIban",  target = "counterpartyIban")
    @Mapping(expression = "java(LocalDateTime.now())", target = "timestamp")
    Transaction toEntity(TransactionCreateRequest dto);
}
