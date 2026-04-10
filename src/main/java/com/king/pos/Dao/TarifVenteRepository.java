package com.king.pos.Dao;

import com.king.pos.Entitys.TarifVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TarifVenteRepository extends JpaRepository<TarifVente, Long> {

    Optional<TarifVente> findByCodeIgnoreCase(String code);

    List<TarifVente> findByActifTrueOrderByNomAsc();

      @Modifying
    @Query("update TarifVente t set t.parDefaut = false where t.parDefaut = true")
    void clearDefaultTarif();
}
