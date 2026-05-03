package com.king.pos.Dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.pos.Entitys.TauxChange;

import java.util.Optional;

public interface TauxChangeRepository extends JpaRepository<TauxChange, Long> {

    Optional<TauxChange> findFirstByActifTrueOrderByDateActivationDesc();

    boolean existsByActifTrue();

    Optional<TauxChange> findTopByActifTrueOrderByDateActivationDescDateCreationDesc();
}
