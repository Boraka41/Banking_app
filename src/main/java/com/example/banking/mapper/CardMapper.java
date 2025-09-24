package com.example.banking.mapper;

import com.example.banking.dto.requests.CreditCardCreateRequest;
import com.example.banking.dto.requests.DebitCardCreateRequest;
import com.example.banking.dto.requests.PrepaidCardCreateRequest;
import com.example.banking.dto.requests.VirtualCardCreateRequest;
import com.example.banking.dto.responses.*;
import com.example.banking.entity.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {

    CreditCard toCreditCard(CreditCardCreateRequest request);
    DebitCard toDebitCard(DebitCardCreateRequest request);
    VirtualCard toVirtualCard(VirtualCardCreateRequest request);
    PrepaidCard toPrepaidCard(PrepaidCardCreateRequest request);

    @Named("toCreditCardResponse")
    @Mapping(source = "account.id", target = "accountId")
    CreditCardResponse toCreditCardResponse(CreditCard card);

    @IterableMapping(qualifiedByName = "toCreditCardResponse")
    List<BaseCardResponse> toCreditCardResponseList(List<CreditCard> cards);

    @Named("toDebitCardResponse")
    @Mapping(source = "account.id", target = "accountId")
    DebitCardResponse toDebitCardResponse(DebitCard card);

    @IterableMapping(qualifiedByName = "toDebitCardResponse")
    List<BaseCardResponse> toDebitCardResponseList(List<DebitCard> cards);

    @Named("toVirtualCardResponse")
    @Mapping(source = "account.id", target = "accountId")
    VirtualCardResponse toVirtualCardResponse(VirtualCard card);

    @IterableMapping(qualifiedByName = "toVirtualCardResponse")
    List<BaseCardResponse> toVirtualCardResponseList(List<VirtualCard> cards);

    @Named("toPrepaidCardResponse")
    @Mapping(source = "account.id", target = "accountId")
    PrepaidCardResponse toPrepaidCardResponse(PrepaidCard card);

    @IterableMapping(qualifiedByName = "toPrepaidCardResponse")
    List<BaseCardResponse> toPrepaidCardResponseList(List<PrepaidCard> cards);

    default String getCardType(Card card) {
        if (card instanceof CreditCard) return "CREDIT";
        if (card instanceof DebitCard) return "DEBIT";
        if (card instanceof VirtualCard) return "VIRTUAL";
        if (card instanceof PrepaidCard) return "PREPAID";
        return "UNKNOWN";
    }
}

