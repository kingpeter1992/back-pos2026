package com.king.pos.Entitys;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "ligne_achat_ligne")
public class CommandeAchatLigne {

   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_achat_id", nullable = false)
    @JsonBackReference
    private CommandeAchat commandeAchat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(precision = 15, scale = 2)
private BigDecimal remise;

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal quantiteCommandee;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(precision = 18, scale = 3)
    private BigDecimal quantiteRecue;

    @Column(precision = 18, scale = 2)
    private BigDecimal montantLigne;


    @Column(precision = 18, scale = 6)
private BigDecimal tauxChangeUtilise;

@Column(precision = 18, scale = 2)
private BigDecimal prixUnitaireFc;

@Column(precision = 18, scale = 2)
private BigDecimal prixUnitaireUsd;

@Column(precision = 18, scale = 2)
private BigDecimal montantLigneFc;

@Column(precision = 18, scale = 2)
private BigDecimal montantLigneUsd;

@PrePersist
@PreUpdate
public void preSave() {
    if (quantiteRecue == null) quantiteRecue = BigDecimal.ZERO;
    if (quantiteCommandee == null) quantiteCommandee = BigDecimal.ZERO;
    if (prixUnitaire == null) prixUnitaire = BigDecimal.ZERO;
    if (remise == null) remise = BigDecimal.ZERO;
    if (tauxChangeUtilise == null) tauxChangeUtilise = BigDecimal.ZERO;

    BigDecimal sousTotal = quantiteCommandee
            .multiply(prixUnitaire)
            .subtract(remise);

    if (sousTotal.compareTo(BigDecimal.ZERO) < 0) {
        sousTotal = BigDecimal.ZERO;
    }

    montantLigne = sousTotal;

    if (prixUnitaireFc == null) {
        prixUnitaireFc = prixUnitaire;
    }

    if (montantLigneFc == null) {
        montantLigneFc = sousTotal;
    }

    if (prixUnitaireUsd == null) {
        prixUnitaireUsd = BigDecimal.ZERO;
    }

    if (montantLigneUsd == null) {
        montantLigneUsd = BigDecimal.ZERO;
    }
}
}