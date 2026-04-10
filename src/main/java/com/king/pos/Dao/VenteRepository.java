package com.king.pos.Dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.king.pos.Entitys.Vente;

public interface VenteRepository extends JpaRepository<Vente, Long> {
}