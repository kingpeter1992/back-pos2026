package com.king.pos.Dto.Response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TarificationResponse {
    private Long produitId;
    private String produitNom;
    private String codeBarres;

    private Long categorieId;
    private String categorieNom;

    private Long tarifVenteId;
    private String tarifCode;
    private String tarifNom;

    private BigDecimal pmp;
    private BigDecimal tauxMarge;
    private BigDecimal tauxRemiseMax;
    private BigDecimal tauxRemiseAppliquee;

    private BigDecimal prixBrut;
    private BigDecimal montantRemise;
    private BigDecimal prixNet;

    private String modeArrondi;
    private BigDecimal stockDisponible;
}
