package com.king.pos.Dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.king.pos.Entitys.Depot;

public interface DepotRepository extends JpaRepository<Depot, Long> {

 List<Depot> findAllByOrderByNomAsc();

    List<Depot> findByActifTrueOrderByNomAsc();

    Optional<Depot> findFirstByParDefautTrueAndActifTrue();

    boolean existsByNomIgnoreCase(String nom);

}
