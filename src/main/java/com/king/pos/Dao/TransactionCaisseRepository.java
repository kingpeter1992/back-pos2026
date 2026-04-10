package com.king.pos.Dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.king.pos.Entitys.TransactionCaisse;
import com.king.pos.Entitys.TypeTransaction;
import com.king.pos.enums.Devise;
@Repository
public interface TransactionCaisseRepository extends JpaRepository<TransactionCaisse,Long> {

        List<TransactionCaisse> findBySessionIdOrderByDateTransactionDesc(Long sessionId);

    @Query("""
        select t from TransactionCaisse t
        where t.session.id = :sessionId
        order by t.dateTransaction desc
    """)
    List<TransactionCaisse> historique(@Param("sessionId") Long sessionId);


@Query("""
select t from TransactionCaisse t
left join fetch t.session s
left join fetch t.client c
where t.dateTransaction >= :from and t.dateTransaction < :to
order by t.dateTransaction desc
""")
List<TransactionCaisse> findByDateRange(@Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to);
List<TransactionCaisse> findByClientIdAndTypeAndDateTransactionBetween(
        Long clientId,
        TypeTransaction type,
        LocalDateTime dateFrom,
        LocalDateTime dateTo
);

List<TransactionCaisse> findByClientIdAndDateTransactionBetween(
    Long clientId,
    LocalDateTime from,
    LocalDateTime to
);


@Query("""
    SELECT t FROM TransactionCaisse t
    WHERE t.client.id = :clientId
      AND t.type = :type
      AND t.devise = :devise
    ORDER BY t.dateTransaction ASC
""")
List<TransactionCaisse> findByClientAndTypeAndDeviseOrderByDateAsc(
        Long clientId,
        TypeTransaction type,
        Devise devise
);


@Query("""
  SELECT COALESCE(SUM(t.montant),0)
  FROM TransactionCaisse t
  WHERE t.client.id = :clientId
    AND t.devise = :devise
    AND t.type = :type
""")
double totalByType(Long clientId, Devise devise, TypeTransaction type);


List<TransactionCaisse> findByClient_IdAndDeviseAndDateTransactionBetweenOrderByDateTransactionAsc(
    Long clientId,
    Devise devise,
    LocalDateTime from,
    LocalDateTime to
);  
List<TransactionCaisse> findByClient_IdAndDeviseOrderByDateTransactionAsc(Long clientId, Devise devise);
 List<TransactionCaisse> findByClient_IdAndDeviseAndTypeOrderByDateTransactionAsc(
      Long clientId,
      Devise devise,
      TypeTransaction type
  );


   @Query("""
    SELECT COALESCE(SUM(t.montant),0)
    FROM TransactionCaisse t
    WHERE t.client.id = :clientId AND t.devise = :devise AND t.type = :type
  """)
  double sumMontantByClientAndDeviseAndType(Long clientId, Devise devise, TypeTransaction type);

}
