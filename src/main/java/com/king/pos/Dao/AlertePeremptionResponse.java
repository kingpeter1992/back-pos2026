package com.king.pos.Dao;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.king.pos.enums.StatutPeremption;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertePeremptionResponse {

    private Long lotId;
    private Long id;

    private Long produitId;
    private String produitNom;
    private String codeBarres;

    private Long depotId;
    private String depotNom;

    private BigDecimal quantiteInitiale;
    private BigDecimal quantiteDisponible;

    private BigDecimal prixUnitaire;
    private BigDecimal fraisUnitaire;
    private BigDecimal coutUnitaireFinal;

    private BigDecimal tauxChangeUtilise;

    private BigDecimal prixUnitaireFc;
    private BigDecimal prixUnitaireUsd;

    private BigDecimal fraisUnitaireFc;
    private BigDecimal fraisUnitaireUsd;

    private BigDecimal coutUnitaireFinalFc;
    private BigDecimal coutUnitaireFinalUsd;

    private BigDecimal valeurLotFc;
    private BigDecimal valeurLotUsd;

    private LocalDate dateEntree;
    private LocalDate datePeremption;

    private long joursRestants;
    private StatutPeremption statutPeremption;
    private String niveauAlerte;

    private String referenceDocument;
    private String sourceDocument;
    private Long sourceDocumentId;

    private LocalDate dateCreation;
    private LocalDate dateModification;

    private String numeroLot;
}