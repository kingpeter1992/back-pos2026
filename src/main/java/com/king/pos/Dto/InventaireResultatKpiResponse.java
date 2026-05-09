package com.king.pos.Dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class InventaireResultatKpiResponse {

    private Integer totalArticles;
    private Integer articlesComptes;
    private Integer articlesAvecEcart;
    private Integer articlesSansEcart;

    private BigDecimal pourcentageArticlesAvecEcart;

    private BigDecimal totalStockTheorique;
    private BigDecimal totalStockComptee;
    private BigDecimal totalEcartQuantite;

    private BigDecimal valeurTheoriqueCDF;
    private BigDecimal valeurCompteeCDF;
    private BigDecimal valeurEcartCDF;

    private BigDecimal valeurEcartPositifCDF;
    private BigDecimal valeurEcartNegatifCDF;

    private BigDecimal valeurTheoriqueUSD;
    private BigDecimal valeurCompteeUSD;
    private BigDecimal valeurEcartUSD;

    private BigDecimal pourcentageEcartValeur;

    private Integer bordereauxTotal;
    private Integer bordereauxMisAJourStock;

    private Boolean stockTotalementMisAJour;
}
