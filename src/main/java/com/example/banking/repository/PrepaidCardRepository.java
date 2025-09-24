package com.example.banking.repository;

import com.example.banking.entity.PrepaidCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrepaidCardRepository extends JpaRepository<PrepaidCard,Long> {

}
