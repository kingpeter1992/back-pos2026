package com.king.pos.Dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.pos.Entitys.TransactionStock;

public interface TransactionStockRepository extends JpaRepository<TransactionStock, Long> {

    List<TransactionStock> findAllByOrderByDateTransactionDesc();

    List<TransactionStock> findByReferenceDocumentAndSourceDocument(String reference, String source);
}