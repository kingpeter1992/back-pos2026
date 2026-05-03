package com.king.pos.Dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.king.pos.Entitys.Produit;

import java.util.List;
import java.util.Optional;

public interface ProduitRepository extends JpaRepository<Produit, Long> {
    Optional<Produit> findByCodeBarres(String codeBarres);
    List<Produit> findByNomContainingIgnoreCase(String nom);
    boolean existsByCodeBarres(String codeBarres);
    boolean existsByCategorieId(Long id);
    

    @EntityGraph(attributePaths = {
            "categorie",
            "images",
            "produitFournisseurs",
            "produitFournisseurs.fournisseur"
    })
    Optional<Produit> findWithDetailsById(Long id);


       @Query("select p.codeBarres from Produit p where p.id = :id")
    Optional<String> findCodeBarresById(@Param("id") Long id);
    
}
