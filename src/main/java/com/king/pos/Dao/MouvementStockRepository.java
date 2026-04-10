package com.king.pos.Dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.king.pos.Dto.Response.MouvementStockView;
import com.king.pos.Entitys.MouvementStock;


public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {
     @Query("""
        select new com.king.pos.Dto.Response.MouvementStockView(
            m.id,
            m.dateMouvement,
            m.typeMouvement,
            p.id,
            p.nom,
            p.codeBarres,
            d.id,
            d.nom,
            m.quantite,
            m.prixUnitaireEntree,
            m.fraisUnitaire,
            m.coutUnitaireFinal,
            m.ancienStock,
            m.ancienPmp,
            m.nouveauStock,
            m.nouveauPmp,
            m.referenceDocument,
            m.libelle
        )
        from MouvementStock m
        join m.produit p
        left join m.depot d
        order by m.dateMouvement desc, m.id desc
    """)
    List<MouvementStockView> findAllView();
}
