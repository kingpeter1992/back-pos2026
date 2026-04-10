package com.king.pos.Dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TarifCategorieProduitRequest {
    private Long tarifVenteId;
    private Long categorieId;
    private BigDecimal tauxMarge;
    private BigDecimal tauxRemiseMax;
    private Boolean actif;
    private String modeArrondi;
}