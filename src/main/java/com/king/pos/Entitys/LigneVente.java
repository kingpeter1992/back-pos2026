package com.king.pos.Entitys;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "ligne_vente")
public class LigneVente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vente_id", nullable = false)
    private Vente vente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(nullable = false)
    private BigDecimal quantite;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal prixUnitaire;

    @Column(precision = 12, scale = 2)
    private BigDecimal remise;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal sousTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarif_vente_id")
    private TarifVente tarifVente;

    @Column(precision = 18, scale = 6)
    private BigDecimal pmpAuMomentVente;

    @Column(precision = 18, scale = 4)
    private BigDecimal tauxMarge;

    @Column(precision = 18, scale = 4)
    private BigDecimal tauxRemiseMax;

    @Column(precision = 18, scale = 4)
    private BigDecimal tauxRemiseAppliquee;

    @Column(precision = 18, scale = 6)
    private BigDecimal prixBrut;

    @Column(precision = 18, scale = 6)
    private BigDecimal montantRemise;

    @Column(precision = 18, scale = 6)
    private BigDecimal prixUnitaireVente;

        @Column(precision = 18, scale = 2)
    private BigDecimal tauxTva;
}