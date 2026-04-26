package com.king.pos.Dao;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.king.pos.Dto.Response.InventaireVarianceResponse;
import com.king.pos.Entitys.Inventaire;
import com.king.pos.enums.StatutInventaire;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InventaireRepository extends JpaRepository<Inventaire, Long> {
   
    Optional<Inventaire> findTopByOrderByIdDesc();

    List<Inventaire> findByStatut(StatutInventaire statut);

    @Override
    @EntityGraph(attributePaths = {"depot", "locator"})
    List<Inventaire> findAll();

    @Override
    @EntityGraph(attributePaths = {"depot", "locator"})
    Optional<Inventaire> findById(Long id);
}
