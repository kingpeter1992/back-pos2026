package com.king.pos.Dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.pos.Entitys.RequisitionHistorique;

public interface HistoriqueRepository extends JpaRepository<RequisitionHistorique, Long> {

    List<RequisitionHistorique> findByDateBetweenOrderByDateHeureDesc(LocalDate dateFrom, LocalDate dateTo);
    
}
