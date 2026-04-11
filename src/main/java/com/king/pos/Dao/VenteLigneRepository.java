package com.king.pos.Dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.king.pos.Entitys.LigneVente;

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
    
}
