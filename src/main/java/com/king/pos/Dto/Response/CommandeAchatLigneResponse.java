package com.king.pos.Dto.Response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommandeAchatLigneResponse {

    private Long id;

    private Long produitId;
    private String produitNom;
    private String codeBarres;

    private BigDecimal quantite;
    private BigDecimal quantiteCommandee;
    private BigDecimal quantiteRecue;

    private BigDecimal prixUnitaire;
    private BigDecimal remise;
    private BigDecimal montantLigne;

    // NOUVEAU
    private BigDecimal tauxChangeUtilise;

    private BigDecimal prixUnitaireFc;
    private BigDecimal prixUnitaireUsd;

    private BigDecimal montantLigneFc;
    private BigDecimal montantLigneUsd;
}