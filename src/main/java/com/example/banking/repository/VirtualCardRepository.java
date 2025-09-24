package com.example.banking.repository;

import com.example.banking.entity.VirtualCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VirtualCardRepository extends JpaRepository<VirtualCard,Long> {

}
