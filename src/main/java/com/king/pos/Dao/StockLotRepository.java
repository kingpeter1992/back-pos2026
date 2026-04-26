package com.king.pos.Dao;


import com.king.pos.Entitys.StockLot;
import com.king.pos.enums.StatutPeremption;
import com.king.pos.Entitys.Depot;
import com.king.pos.Entitys.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface StockLotRepository extends JpaRepository<StockLot, Long> {

    List<StockLot> findByQuantiteDisponibleGreaterThan(BigDecimal quantite);

    List<StockLot> findByProduitAndDepotAndQuantiteDisponibleGreaterThan(
            Produit produit,
            Depot depot,
            BigDecimal quantite
    );

    @Query("""
        select sl
        from StockLot sl
        where sl.produit.id = :produitId
          and sl.depot.id = :depotId
          and sl.quantiteDisponible > 0
        order by
            case when sl.datePeremption is null then 1 else 0 end,
            sl.datePeremption asc,
            sl.dateEntree asc,
            sl.id asc
    """)
    List<StockLot> findLotsDisponiblesPourSortie(
            @Param("produitId") Long produitId,
            @Param("depotId") Long depotId
    );

@Query("""
    select sl
    from StockLot sl
    join fetch sl.produit
    join fetch sl.depot
    where sl.statutPeremption = :statut
      and sl.datePeremption is not null
""")
List<StockLot> findLotsEnAlerte(@Param("statut") StatutPeremption statut);

    List<StockLot> findByQuantiteDisponibleGreaterThanAndStatutPeremptionNot(
            BigDecimal quantite,
            StatutPeremption statut
    );

    long countByQuantiteDisponibleGreaterThan(BigDecimal quantite);

    long countByQuantiteDisponibleGreaterThanAndStatutPeremption(BigDecimal quantite, StatutPeremption statut);

    long countByQuantiteDisponibleGreaterThanAndStatutPeremptionNot(BigDecimal quantite, StatutPeremption statut);

@Query("""
    select sl
    from StockLot sl
    join fetch sl.produit
    join fetch sl.depot
    where sl.datePeremption is not null
""")
List<StockLot> findLotsEnAlerte();

List<StockLot> findByDepotId(Long id);

    
}