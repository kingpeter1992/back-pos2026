package com.king.pos.Dto.Response;

import lombok.*;

import java.math.BigDecimal;

import jakarta.persistence.Column;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockProduitView {

    private Long stockId;
    private Long produitId;
    private String nomProduit;
    private String codeBarre;
    private Long categorieId;
    private String categorie;
    private Long depotId;
    private String nomDepot;

    private Long locatorId;
    private String locatorCode;

    private BigDecimal quantiteDisponible;
    private BigDecimal pmp;
    private BigDecimal valeurStock;

    private BigDecimal stockMaximum;
    private BigDecimal stockMinimum;

    private String statutStock;
    
    private BigDecimal tauxChangeUtilise;

    private BigDecimal pmpFc;

    private BigDecimal pmpUsd;

    private BigDecimal valeurStockFc;

    private BigDecimal valeurStockUsd;


    // ===== TARIFICATION / MARGE =====
    private Long tarifVenteId;
    private String tarifCode;
    private String tarifNom;

    private BigDecimal tauxMarge;         // ex: 25
    private BigDecimal margeUnitaire;     // ex: 250 si PMP=1000 et marge=25%
    private BigDecimal prixVenteUnitaire; // ex: 1250
    private BigDecimal margeTotaleStock;  // margeUnitaire * quantite
}
