package com.king.pos.Dto.Response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InventaireArticleResponse {
    private Long id;
    private Long produitId;
    private String codeArticle;
    private String designation;
    private String depotNom;
    private String locatorCode;
    private BigDecimal stockTheorique;
    private BigDecimal stockPhysiqueRetenu;
    private BigDecimal ecartQuantite;
    private BigDecimal valeurEcart;
    private Boolean compte;
    private String dernierCommentaire;
    private LocalDateTime derniereDateComptage;
}
