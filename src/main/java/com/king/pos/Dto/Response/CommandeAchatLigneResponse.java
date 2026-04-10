package com.king.pos.Dto.Response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class CommandeAchatLigneResponse {
    private Long id;
    private Long produitId;
    private String produitNom;
    private String codeBarres;
    private BigDecimal quantiteCommandee;
    private BigDecimal quantiteRecue;
    private BigDecimal prixUnitaire;
    private BigDecimal montantLigne;
}
