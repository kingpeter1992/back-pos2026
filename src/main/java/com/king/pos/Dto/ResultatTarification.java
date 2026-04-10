package com.king.pos.Dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ResultatTarification {
    private Long produitId;
    private String produitNom;
    private Long categorieId;
    private String categorieNom;

    private Long tarifVenteId;
    private String tarifNom;

    private BigDecimal pmp;
    private BigDecimal tauxMarge;
    private BigDecimal tauxRemiseMax;
    private BigDecimal tauxRemiseAppliquee;

    private BigDecimal prixBrut;
    private BigDecimal montantRemise;
    private BigDecimal prixNet;
}
