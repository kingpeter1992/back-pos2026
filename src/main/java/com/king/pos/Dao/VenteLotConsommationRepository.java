package com.king.pos.Dao;

import com.king.pos.Entitys.VenteLotConsommation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenteLotConsommationRepository extends JpaRepository<VenteLotConsommation, Long> {

    List<VenteLotConsommation> findByVenteIdOrderByIdAsc(Long venteId);

    List<VenteLotConsommation> findByLigneVenteIdOrderByIdAsc(Long ligneVenteId);
}