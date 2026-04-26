package com.king.pos.Dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.king.pos.Entitys.LigneVente;
import com.king.pos.enums.StatutVente;

public interface VenteLigneRepository extends JpaRepository<LigneVente, Long> {

 @Query("""
        select lv.produit.id, sum(lv.quantite)
        from LigneVente lv
        where lv.vente.dateVente between :dateDebut and :dateFin
        group by lv.produit.id
    """)
    List<Object[]> getQuantitesVenduesParProduit(
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin
    );

    @Query("""
        select max(v.dateVente)
        from LigneVente lv
        join lv.vente v
        where lv.produit.id = :produitId
          and v.statut = :statut
    """)
    LocalDateTime findDerniereDateVenteByProduitId(
            @Param("produitId") Long produitId,
            @Param("statut") StatutVente statut
    );
}
