package com.king.pos.Dao;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.pos.Entitys.Fournisseur;

public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {

       Optional<Fournisseur> findByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCase(String nom);

    List<Fournisseur> findByNomContainingIgnoreCaseOrTelephoneContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String nom, String telephone, String email
    );
}
