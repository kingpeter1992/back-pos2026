package com.king.pos.Dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.king.pos.Entitys.Produit;

import java.util.List;
import java.util.Optional;

public interface ProduitRepository extends JpaRepository<Produit, Long> {
    Optional<Produit> findByCodeBarres(String codeBarres);
    List<Produit> findByNomContainingIgnoreCase(String nom);
    boolean existsByCodeBarres(String codeBarres);
    boolean existsByCategorieId(Long id);
}
