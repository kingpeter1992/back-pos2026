package com.king.pos.Dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.king.pos.Entitys.Caisse;
@Repository
public interface CaisseRepository extends JpaRepository<Caisse,Long>{

    
}
