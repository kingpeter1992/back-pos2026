package com.king.pos.Dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.king.pos.Entitys.Vente;

public interface VenteRepository extends JpaRepository<Vente, Long> {
        boolean existsByVenteOrigineId(Long venteOrigineId);

        @Query("""
                        SELECT DISTINCT v
                        FROM Vente v
                        LEFT JOIN FETCH v.lignes l
                        LEFT JOIN FETCH l.produit p
                        LEFT JOIN FETCH p.categorie c
                        LEFT JOIN FETCH v.depot d
                        WHERE v.dateVente BETWEEN :dateDebut AND :dateFin
                        AND (:depotId IS NULL OR d.id = :depotId)
                        AND (:caissier IS NULL OR v.caissier = :caissier)
                        AND (:devise IS NULL OR v.devise = :devise)
                        """)
        List<Vente> findRapportVentes(
                        @Param("dateDebut") LocalDateTime dateDebut,
                        @Param("dateFin") LocalDateTime dateFin,
                        @Param("depotId") Long depotId,
                        @Param("caissier") String caissier,
                        @Param("devise") String devise);
}