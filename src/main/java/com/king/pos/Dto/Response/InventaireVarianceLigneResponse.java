package com.king.pos.Dto.Response;

import java.math.BigDecimal;

import com.king.pos.enums.TypeVariance;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventaireVarianceLigneResponse {
    private Long id;
    private Long produitId;
    private String produitNom;
    private String codeBarres;
    private String categorieNom;
    private String depotNom;
    private String locatorCode;

    private BigDecimal stockTheorique;
    private BigDecimal stockPhysiqueRetenu;
    private BigDecimal ecart;
    private BigDecimal pmp;
    private BigDecimal valeurEcart;

    private TypeVariance type;
}
