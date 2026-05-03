package com.king.pos.Dto.Response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class ReceptionAchatLigneResponse {
    private Long id;
    private Long produitId;
    private String produitNom;
    private Long categorieId;
    private String categorieNom;

    private BigDecimal quantiteRecue;
    private BigDecimal prixAchatUnitaire;
    private BigDecimal montantAchat;
    private BigDecimal partFrais;
    private BigDecimal fraisUnitaire;
    private BigDecimal coutUnitaireFinal;
    private BigDecimal tauxChangeUtilise;

private BigDecimal prixAchatUnitaireFc;
private BigDecimal prixAchatUnitaireUsd;

private BigDecimal montantLigneFc;
private BigDecimal montantLigneUsd;

private String commentaire;
private BigDecimal partFraisUsd;
private BigDecimal fraisUnitaireUsd;
private BigDecimal coutUnitaireFinalUsd;
private BigDecimal montantFinalLigneFc;
private BigDecimal montantFinalLigneUsd;
private LocalDate datePeremption;
private String numeroLot;
}