package com.king.pos.Entitys;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.king.pos.enums.TypeMouvementStock;

@Entity
@Data @Builder
@Table(name = "mouvement_stock")
public class MouvementStock {

   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateMouvement;

    @Enumerated(EnumType.STRING)
    private TypeMouvementStock typeMouvement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id")
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id")
    private Depot depot;

    @Column(precision = 18, scale = 3)
    private BigDecimal quantite;

    @Column(precision = 18, scale = 6)
    private BigDecimal prixUnitaireEntree;

    @Column(precision = 18, scale = 6)
    private BigDecimal fraisUnitaire;

    @Column(precision = 18, scale = 6)
    private BigDecimal coutUnitaireFinal;

    @Column(precision = 18, scale = 3)
    private BigDecimal ancienStock;

    @Column(precision = 18, scale = 6)
    private BigDecimal ancienPmp;

    @Column(precision = 18, scale = 3)
    private BigDecimal nouveauStock;

    @Column(precision = 18, scale = 6)
    private BigDecimal nouveauPmp;

    @Column(length = 50)
    private String referenceDocument;

    @Column(length = 255)
    private String libelle;

    @PrePersist
    public void prePersist() {
        if (dateMouvement == null) dateMouvement = LocalDateTime.now();
    }
}