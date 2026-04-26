package com.king.pos.Dto.Response;

import java.math.BigDecimal;
import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventaireVarianceResumeResponse {
    private Long inventaireId;
    private String referenceInventaire;
    private String depotNom;
    private String locatorCode;
    private String statut;
    private Boolean varianceLancee;

    private Integer totalLignes;
    private Integer totalEntrees;
    private Integer totalSorties;
    private Integer totalNeant;

    private BigDecimal totalEcartPositif;
    private BigDecimal totalEcartNegatif;
    private BigDecimal totalValeurPositive;
    private BigDecimal totalValeurNegative;
    private BigDecimal totalValeurNette;

    private List<InventaireVarianceLigneResponse> lignes;
}
