package com.king.pos.Dto.Response;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class InventaireBordereauLigneResponse {
    private Long id;
    private Integer numeroLigne;
    private Long inventaireArticleId;
    private String codeArticle;
    private String designation;
    private String depotNom;
    private String locatorCode;
    private BigDecimal quantiteTheorique;
    private BigDecimal quantiteComptee;
    private String commentaire;
}