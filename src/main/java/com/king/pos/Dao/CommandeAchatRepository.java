package com.king.pos.Dao;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.king.pos.Entitys.CommandeAchat;

public interface CommandeAchatRepository extends JpaRepository<CommandeAchat, Long> {

      @Override
    @EntityGraph(attributePaths = {"fournisseur", "lignes", "lignes.produit"})
    List<CommandeAchat> findAll();
}