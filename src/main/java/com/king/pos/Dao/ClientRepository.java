package com.king.pos.Dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.king.pos.Entitys.Client;

@Repository
public interface ClientRepository extends  JpaRepository<Client,Long>{
    @Query(value = "SELECT * FROM client WHERE id = :id", nativeQuery = true)
    Optional<Client> findByIdNative(@Param("id") Long id);


   @Query(value = "SELECT nextval('client_seq')", nativeQuery = true)
Long nextClientId();
}
