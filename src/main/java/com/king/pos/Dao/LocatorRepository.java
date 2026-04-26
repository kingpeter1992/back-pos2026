package com.king.pos.Dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.pos.Entitys.Locator;

public interface LocatorRepository extends JpaRepository<Locator, Long> {

 Optional<Locator> findByDepotIdAndCodeIgnoreCase(Long depotId, String code);

    List<Locator> findByDepotIdAndActifTrueOrderByCodeAsc(Long depotId);

    List<Locator> findByDepotId(Long depotId);

        
}
