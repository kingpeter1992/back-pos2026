package com.king.pos.Dao;


import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.king.pos.Entitys.InventaireBordereauLigne;

import java.util.List;
import java.util.Optional;

public interface InventaireBordereauLigneRepository extends JpaRepository<InventaireBordereauLigne, Long> {


    void deleteByBordereauId(Long bordereauId);

     @EntityGraph(attributePaths = {
            "bordereau",
            "inventaireArticle",
            "inventaireArticle.produit",
            "inventaireArticle.depot",
            "inventaireArticle.locator"
    })
    List<InventaireBordereauLigne> findByBordereauId(Long bordereauId);

    @Override
    @EntityGraph(attributePaths = {
            "bordereau",
            "inventaireArticle",
            "inventaireArticle.produit",
            "inventaireArticle.depot",
            "inventaireArticle.locator"
    })
    Optional<InventaireBordereauLigne> findById(Long id);

    @Modifying
    @Query("""
        update InventaireBordereauLigne l
        set l.retenue = false
        where l.inventaireArticle.id = :inventaireArticleId
    """)
    void clearRetenueForInventaireArticle(@Param("inventaireArticleId") Long inventaireArticleId);
}