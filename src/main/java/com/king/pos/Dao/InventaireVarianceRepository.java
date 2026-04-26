package com.king.pos.Dao;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.king.pos.Entitys.InventaireVariance;

import jakarta.transaction.Transactional;

import java.util.List;

public interface InventaireVarianceRepository extends JpaRepository<InventaireVariance, Long> {

    @EntityGraph(attributePaths = {
            "inventaire",
            "inventaireArticle",
            "inventaireArticle.produit",
            "inventaireArticle.produit.categorie",
            "inventaireArticle.depot",
            "inventaireArticle.locator"
    })
    List<InventaireVariance> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = {
            "inventaire",
            "inventaireArticle",
            "inventaireArticle.produit",
            "inventaireArticle.produit.categorie",
            "inventaireArticle.depot",
            "inventaireArticle.locator"
    })
    List<InventaireVariance> findByInventaireId(Long inventaireId);

    @Modifying
    @Transactional
    void deleteByBordereauId(Long bordereauId);
}