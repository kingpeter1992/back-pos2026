package com.king.pos.Dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequisitionResponse {

    private Long id;

    private Long produitId;
    private String produitNom;
    private String codeBarres;
    private String categorieNom;

    private Long depotId;
    private String depotNom;

    private Long locatorId;
    private String locatorNom;

    private Integer totalDemandes;
    private Integer totalVentes;
    private Integer totalVentesManquees;

    private BigDecimal totalQuantiteVendue;
    private BigDecimal totalQuantiteDemandeeNonVendue;

    private BigDecimal stockActuel;

    private Boolean produitExistant;
    private String origine;

    private String statut;

    private BigDecimal tauxSatisfaction;
    private BigDecimal tauxManque;

    private LocalDate derniereDateDemande;
    private LocalDateTime derniereDemande;

    private String commentaire;
}