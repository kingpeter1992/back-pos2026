package com.king.pos.Dto.Response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TarifCategorieProduitResponse {
    private Long id;
    private Long tarifVenteId;
    private String tarifCode;
    private String tarifNom;
    private Long categorieId;
    private String categorieNom;
    private BigDecimal tauxMarge;
    private BigDecimal tauxRemiseMax;
    private Boolean actif;
    private String modeArrondi;
}