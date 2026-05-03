package com.king.pos.Dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.king.pos.Entitys.TransactionCaisse;
public interface TransactionCaisseRepository extends JpaRepository<TransactionCaisse, Long> {

    List<TransactionCaisse> findBySessionIdOrderByDateTransactionDesc(Long sessionId);

    @Query("""
        SELECT t FROM TransactionCaisse t
        WHERE t.dateTransaction >= :from
        AND t.dateTransaction < :to
        ORDER BY t.dateTransaction DESC
    """)
    List<TransactionCaisse> findByDateRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}