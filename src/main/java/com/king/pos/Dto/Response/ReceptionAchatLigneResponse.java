package com.king.pos.Dto.Response;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ReceptionAchatLigneResponse {
    private Long id;
    private Long produitId;
    private String produitNom;
    private Long categorieId;
    private String categorieNom;

    private BigDecimal quantiteRecue;
    private BigDecimal prixAchatUnitaire;
    private BigDecimal montantAchat;
    private BigDecimal partFrais;
    private BigDecimal fraisUnitaire;
    private BigDecimal coutUnitaireFinal;
}