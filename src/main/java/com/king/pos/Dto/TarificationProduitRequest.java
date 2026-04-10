package com.king.pos.Dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TarificationProduitRequest {
    private Long produitId;
    private Long tarifVenteId;
    private BigDecimal tauxRemiseSaisie; // facultatif
}