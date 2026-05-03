package com.king.pos.Dto;

import java.math.BigDecimal;

import lombok.*;

@Getter
@Setter
public class LigneVenteRequest {

    private Long produitId;
    private BigDecimal quantite;

    private BigDecimal prix;
    private BigDecimal remise;
    private BigDecimal total;

    private BigDecimal prixCDF;
    private BigDecimal remiseCDF;
    private BigDecimal totalCDF;

    private BigDecimal prixUSD;
    private BigDecimal remiseUSD;
    private BigDecimal totalUSD;
        private BigDecimal tauxChange;

}