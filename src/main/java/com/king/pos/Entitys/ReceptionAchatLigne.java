package com.king.pos.Entitys;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceptionAchatLigne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reception_achat_id", nullable = false)
    private ReceptionAchat receptionAchat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal quantiteRecue;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal prixAchatUnitaire;

    @Column(precision = 18, scale = 2)
    private BigDecimal montantAchat;

    @Column(precision = 18, scale = 2)
    private BigDecimal partFrais;

    @Column(precision = 18, scale = 6)
    private BigDecimal fraisUnitaire;

    @Column(precision = 18, scale = 6)
    private BigDecimal coutUnitaireFinal;

    @Column(precision = 18, scale = 2)
    private BigDecimal montantFinalLigne;

    @Column(precision = 18, scale = 3)
    private BigDecimal ancienStock;

    @Column(precision = 18, scale = 6)
    private BigDecimal ancienPmp;

    private LocalDate datePeremption;

    @Column(precision = 18, scale = 3)
    private BigDecimal nouveauStock;

    @Column(precision = 18, scale = 6)
    private BigDecimal nouveauPmp;
    @Column(length = 100)
    private String numeroLot;
    @Column(precision = 18, scale = 6)
    private BigDecimal tauxChangeUtilise;

    @Column(precision = 18, scale = 2)
    private BigDecimal prixAchatUnitaireFc;

    @Column(precision = 18, scale = 2)
    private BigDecimal prixAchatUnitaireUsd;

    @Column(precision = 18, scale = 2)
    private BigDecimal montantLigneFc;

    @Column(precision = 18, scale = 2)
    private BigDecimal montantLigneUsd;

    @Column(precision = 18, scale = 2)
private BigDecimal partFraisUsd;

@Column(precision = 18, scale = 6)
private BigDecimal fraisUnitaireUsd;

@Column(precision = 18, scale = 6)
private BigDecimal coutUnitaireFinalUsd;

@Column(precision = 18, scale = 2)
private BigDecimal montantFinalLigneFc;

@Column(precision = 18, scale = 2)
private BigDecimal montantFinalLigneUsd;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @PrePersist
    @PreUpdate
    public void preSave() {
        if (quantiteRecue == null)
            quantiteRecue = BigDecimal.ZERO;
        if (prixAchatUnitaire == null)
            prixAchatUnitaire = BigDecimal.ZERO;
        if (partFrais == null)
            partFrais = BigDecimal.ZERO;
        if (fraisUnitaire == null)
            fraisUnitaire = BigDecimal.ZERO;
        if (coutUnitaireFinal == null)
            coutUnitaireFinal = BigDecimal.ZERO;
        if (ancienStock == null)
            ancienStock = BigDecimal.ZERO;
        if (ancienPmp == null)
            ancienPmp = BigDecimal.ZERO;
        if (nouveauStock == null)
            nouveauStock = BigDecimal.ZERO;
        if (nouveauPmp == null)
            nouveauPmp = BigDecimal.ZERO;
        if (tauxChangeUtilise == null)
            tauxChangeUtilise = BigDecimal.ZERO;
        if (prixAchatUnitaireFc == null)
            prixAchatUnitaireFc = prixAchatUnitaire;
        if (prixAchatUnitaireUsd == null)
            prixAchatUnitaireUsd = BigDecimal.ZERO;
        if (montantLigneFc == null)
            montantLigneFc = montantAchat;
        if (montantLigneUsd == null)
            montantLigneUsd = BigDecimal.ZERO;
        if (partFraisUsd == null) partFraisUsd = BigDecimal.ZERO;
if (fraisUnitaireUsd == null) fraisUnitaireUsd = BigDecimal.ZERO;
if (coutUnitaireFinalUsd == null) coutUnitaireFinalUsd = BigDecimal.ZERO;
if (montantFinalLigneFc == null) montantFinalLigneFc = montantFinalLigne;
if (montantFinalLigneUsd == null) montantFinalLigneUsd = BigDecimal.ZERO;
        

        montantAchat = quantiteRecue.multiply(prixAchatUnitaire);
        montantFinalLigne = quantiteRecue.multiply(coutUnitaireFinal);
    }
}
