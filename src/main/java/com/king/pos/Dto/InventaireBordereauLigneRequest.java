package com.king.pos.Dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventaireBordereauLigneRequest {
    private Long inventaireArticleId;
    private BigDecimal quantiteComptee;
    private String commentaire;
    private String saisiPar;
}