package com.king.pos.Dao;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.king.pos.Entitys.InventaireLigne;

import java.util.List;

public interface InventaireLigneRepository extends JpaRepository<InventaireLigne, Long> {
     @EntityGraph(attributePaths = {
            "produit",
            "depot",
            "locator",
            "stockLot"
    })
    List<InventaireLigne> findByInventaireId(Long inventaireId);
    long countByInventaireId(Long inventaireId);
}
