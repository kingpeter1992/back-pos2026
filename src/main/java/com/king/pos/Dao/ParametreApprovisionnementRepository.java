package com.king.pos.Dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.pos.Entitys.ParametreApprovisionnement;

public interface ParametreApprovisionnementRepository extends JpaRepository<ParametreApprovisionnement, Long> {

    Optional<ParametreApprovisionnement> findFirstByActifTrue();

}
