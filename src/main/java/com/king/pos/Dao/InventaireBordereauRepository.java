package com.king.pos.Dao;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.king.pos.Entitys.InventaireBordereau;
import com.king.pos.enums.StatutBordereauInventaire;

import java.util.List;
import java.util.Optional;

public interface InventaireBordereauRepository extends JpaRepository<InventaireBordereau, Long> {
    Optional<InventaireBordereau> findTopByOrderByIdDesc();

    @EntityGraph(attributePaths = {
            "inventaire",
            "depot",
            "locator"
    })
    List<InventaireBordereau> findByInventaireId(Long inventaireId);

    @Override
    @EntityGraph(attributePaths = {
            "inventaire",
            "depot",
            "locator"
    })
    Optional<InventaireBordereau> findById(Long id);

boolean existsByInventaireIdAndStatutNot(
        Long inventaireId,
        StatutBordereauInventaire statut
);
}
