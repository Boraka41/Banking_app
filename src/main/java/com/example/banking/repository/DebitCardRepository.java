package com.example.banking.repository;

import com.example.banking.entity.DebitCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DebitCardRepository extends JpaRepository<DebitCard,Long> {

}
