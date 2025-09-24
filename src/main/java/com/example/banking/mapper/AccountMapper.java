package com.example.banking.mapper;

import com.example.banking.dto.requests.AccountCreateRequest;
import com.example.banking.dto.responses.AccountResponse;
import com.example.banking.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(source = "user.id", target = "userId")
    AccountResponse toResponseDto(Account account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Account toEntity(AccountCreateRequest req);

    List<AccountResponse> toResponseList(List<Account> accounts);

}
