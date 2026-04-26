package com.king.pos.Dao;

import com.king.pos.Entitys.TarifCategorieProduit;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TarifCategorieProduitRepository extends JpaRepository<TarifCategorieProduit, Long> {

    Optional<TarifCategorieProduit> findByTarifVenteIdAndCategorieIdAndActifTrue(Long tarifVenteId, Long categorieId);

    List<TarifCategorieProduit> findByTarifVenteIdOrderByCategorieNomAsc(Long tarifVenteId);

    boolean existsByTarifVenteIdAndCategorieId(Long tarifVenteId, Long categorieId);


@Query("""
    SELECT t FROM TarifCategorieProduit t
    WHERE t.actif = true
    AND t.dateCreation = (
        SELECT MAX(t2.dateCreation)
        FROM TarifCategorieProduit t2
        WHERE t2.categorie.id = t.categorie.id
        AND t2.actif = true
    )
""")
List<TarifCategorieProduit> findLatestActifByCategorie();
}
