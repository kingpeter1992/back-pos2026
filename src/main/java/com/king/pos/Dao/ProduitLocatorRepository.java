package com.king.pos.Dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.pos.Entitys.ProduitLocator;

import java.util.List;
import java.util.Optional;

public interface ProduitLocatorRepository extends JpaRepository<ProduitLocator, Long> {

    Optional<ProduitLocator> findByProduitIdAndDepotId(Long produitId, Long depotId);

    List<ProduitLocator> findByDepotId(Long id);
}