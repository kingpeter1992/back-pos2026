package com.king.pos.Dto.Response;


import lombok.*;

import java.math.BigDecimal;




@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvisionStockResponse {

    private Long produitId;
    private String codeBarres;
    private String produitNom;
    private String categorieNom;

    private BigDecimal quantiteDisponible;

    private BigDecimal tauxChangeUtilise;

    private BigDecimal pmp;
    private BigDecimal pmpFc;
    private BigDecimal pmpUsd;

    private BigDecimal valeurStock;
    private BigDecimal valeurStockFc;
    private BigDecimal valeurStockUsd;

    private Integer joursSansVente;
    private BigDecimal tauxProvision;

    private BigDecimal montantProvision;
    private BigDecimal montantProvisionFc;
    private BigDecimal montantProvisionUsd;

    private String niveauRisque;
}