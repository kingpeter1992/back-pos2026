package com.king.pos.Dto;


import lombok.*;

import java.math.BigDecimal;

import com.king.pos.Entitys.Depot;
import com.king.pos.Entitys.Produit;
import com.king.pos.enums.TypeMouvementStock;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionStockRequest {

    private TypeMouvementStock  typeTransaction;
    private Produit produit;
    private Depot depot;

    private BigDecimal quantite;

    private BigDecimal prixUnitaire;
    private BigDecimal fraisUnitaire;
    private BigDecimal coutUnitaireFinal;

    private String referenceDocument;
    private String sourceDocument;
    private Long sourceDocumentId;
    private String libelle;
    private String utilisateur;
    private BigDecimal tauxChangeUtilise;

private BigDecimal prixUnitaireFc;
private BigDecimal prixUnitaireUsd;

private BigDecimal fraisUnitaireFc;
private BigDecimal fraisUnitaireUsd;

private BigDecimal coutUnitaireFinalFc;
private BigDecimal coutUnitaireFinalUsd;

private BigDecimal montantLigneFc;
private BigDecimal montantLigneUsd;



}