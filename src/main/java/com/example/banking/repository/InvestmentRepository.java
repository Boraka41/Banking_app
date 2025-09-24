package com.example.banking.repository;

import com.example.banking.entity.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment,Long> {

    List<Investment> findByAccountId(Long accountId);
}
