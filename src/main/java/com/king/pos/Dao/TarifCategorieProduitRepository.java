package com.king.pos.Dao;

import com.king.pos.Entitys.TarifCategorieProduit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TarifCategorieProduitRepository extends JpaRepository<TarifCategorieProduit, Long> {

    Optional<TarifCategorieProduit> findByTarifVenteIdAndCategorieIdAndActifTrue(Long tarifVenteId, Long categorieId);

    List<TarifCategorieProduit> findByTarifVenteIdOrderByCategorieNomAsc(Long tarifVenteId);

    boolean existsByTarifVenteIdAndCategorieId(Long tarifVenteId, Long categorieId);
}
