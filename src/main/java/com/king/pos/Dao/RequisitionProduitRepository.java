package com.king.pos.Dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.king.pos.Entitys.RequisitionProduit;

public interface RequisitionProduitRepository extends JpaRepository<RequisitionProduit, Long>{

    Optional<RequisitionProduit> findByProduitId(Long produitId);

    Optional<RequisitionProduit> findByProduitIdAndDepotIdAndLocatorId(Long produitId, Long depotId, Long locatorId);
    
}
