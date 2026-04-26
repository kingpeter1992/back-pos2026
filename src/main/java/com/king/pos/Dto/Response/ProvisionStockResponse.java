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
    private BigDecimal pmp;
    private BigDecimal valeurStock;

    private Integer joursSansVente;
    private BigDecimal tauxProvision;
    private BigDecimal montantProvision;

    private String niveauRisque; // FAIBLE, MOYEN, ELEVE, TOTAL
}
