package com.king.pos.Dao;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.king.pos.Dto.ReceptionAchatLigneRequest;
import com.king.pos.Entitys.Depot;
import com.king.pos.Entitys.ReceptionAchat;

public interface ReceptionAchatRepository extends JpaRepository<ReceptionAchat, Long> {
 @Query("""
        select distinct r
        from ReceptionAchat r
        left join fetch r.depot
        left join fetch r.fournisseur
        left join fetch r.commandeAchat
        left join fetch r.lignes l
        left join fetch l.produit p
        left join fetch p.categorie
        order by r.dateReception desc
    """)
    List<ReceptionAchat> findAllWithReferences();

    @Query("""
        select distinct r
        from ReceptionAchat r
        left join fetch r.depot
        left join fetch r.fournisseur
        left join fetch r.commandeAchat
        left join fetch r.lignes l
        left join fetch l.produit p
        left join fetch p.categorie
        where r.id = :id
    """)
    Optional<ReceptionAchat> findByIdWithDetails(@Param("id") Long id);

    @Query("""
        select distinct r
        from ReceptionAchat r
        left join fetch r.depot
        left join fetch r.fournisseur
        left join fetch r.commandeAchat
        left join fetch r.lignes l
        left join fetch l.produit p
        left join fetch p.categorie
        where r.commandeAchat.id = :commandeId
        order by r.dateReception desc
    """)
    List<ReceptionAchat> findByCommandeAchatIdOrderByDateReceptionDesc(@Param("commandeId") Long commandeId);

}