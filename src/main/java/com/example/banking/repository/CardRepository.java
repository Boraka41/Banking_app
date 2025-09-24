package com.example.banking.repository;

import com.example.banking.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card,Long> {

    List<Card> findByAccount_Id(Long accountId);

    List<Card> findByAccount_User_KeycloakId(String keycloakId);

}
