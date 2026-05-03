package com.king.pos.Dto;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionStockView {

    private Long id;
    private LocalDateTime dateTransaction;

    private String typeTransaction;

    private Long produitId;
    private String produitNom;

    private Long depotId;
    private String depotNom;

    private BigDecimal quantite;

    private BigDecimal stockAvant;
    private BigDecimal stockApres;

    private BigDecimal pmpAvant;
    private BigDecimal pmpApres;

    private BigDecimal prixUnitaire;
    private BigDecimal fraisUnitaire;
    private BigDecimal coutUnitaireFinal;

    private String referenceDocument;
    private String sourceDocument;
    private Long sourceDocumentId;

    private String libelle;
    private String utilisateur;

    private BigDecimal tauxChangeUtilise;

private BigDecimal pmpFc;
private BigDecimal pmpUsd;

private BigDecimal valeurStockFc;
private BigDecimal valeurStockUsd;

private BigDecimal valeurMouvementFc;
private BigDecimal valeurMouvementUsd;

private BigDecimal coutUnitaireFinalFc;
private BigDecimal coutUnitaireFinalUsd;
}