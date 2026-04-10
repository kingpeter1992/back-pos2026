package com.king.pos.Dao;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.king.pos.Entitys.CaisseSession;

import jakarta.persistence.LockModeType;

public interface CaisseSessionRepository extends JpaRepository<CaisseSession, Long> {

  Optional<CaisseSession> findByDateJourAndStatut(LocalDate dateJour, String statut);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from CaisseSession s where s.id = :id")
  Optional<CaisseSession> lockById(@Param("id") Long id);


  
}
