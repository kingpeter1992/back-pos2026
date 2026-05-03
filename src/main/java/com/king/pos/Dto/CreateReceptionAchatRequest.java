package com.king.pos.Dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.king.pos.enums.Devise;

@Data
public class CreateReceptionAchatRequest {
    private Long commandeAchatId;
    private Long fournisseurId;
    private Long depotId;

    private LocalDate dateReception;
    private String referenceBonReception;
    private String observateur;

    private String numeroLivraison;
    private String numeroFactureFournisseur;

    private Devise devise;
    private BigDecimal taux;

    private BigDecimal tauxChangeUtilise;

    private BigDecimal fraisTransport;
    private BigDecimal fraisDouane;
    private BigDecimal fraisManutention;
    private BigDecimal autresFrais;

    private BigDecimal montantMarchandiseFc;
    private BigDecimal montantMarchandiseUsd;
    private BigDecimal montantFraisFc;
    private BigDecimal montantFraisUsd;
    private BigDecimal montantTotalFc;
    private BigDecimal montantTotalUsd;

    private String observation;

    private List<ReceptionAchatLigneRequest> lignes;
}