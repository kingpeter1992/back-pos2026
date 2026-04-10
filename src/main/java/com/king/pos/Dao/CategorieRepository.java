package com.king.pos.Dao;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.pos.Entitys.Categorie;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {
   boolean existsByNomIgnoreCase(String nom);
    Optional<Categorie> findByNomIgnoreCase(String nom);
    List<Categorie> findByActifTrueOrderByNomAsc();
    boolean existsById(Long id);
}
