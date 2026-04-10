package com.king.pos.Dao;


import org.springframework.data.jpa.repository.JpaRepository;

import com.king.pos.Entitys.ProduitFournisseur;

import java.util.List;
import java.util.Optional;

public interface ProduitFournisseurRepository extends JpaRepository<ProduitFournisseur, Long> {

    List<ProduitFournisseur> findByProduitId(Long produitId);

    List<ProduitFournisseur> findByFournisseurId(Long fournisseurId);

    Optional<ProduitFournisseur> findByProduitIdAndFournisseurId(Long produitId, Long fournisseurId);

    boolean existsByProduitIdAndFournisseurId(Long produitId, Long fournisseurId);

    List<ProduitFournisseur> findByProduitIdAndActifTrue(Long produitId);

    Optional<ProduitFournisseur> findByProduitIdAndFournisseurPrincipalTrue(Long produitId);
}