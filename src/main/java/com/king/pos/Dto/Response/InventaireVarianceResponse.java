package com.king.pos.Dto.Response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.king.pos.enums.TypeVariance;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventaireVarianceResponse {

    private Long id;

    private Long inventaireId;

    private Long inventaireArticleId;

    private Long produitId;
    private String produitNom;
    private String codeBarres;

    private Long categorieId;
    private String categorieNom;

    private Long depotId;
    private String depotNom;

    private Long locatorId;
    private String locatorCode;

    private BigDecimal stockTheorique;
    private BigDecimal stockPhysiqueRetenu;
    private BigDecimal ecart;
    private BigDecimal pmp;
    private BigDecimal valeurEcart;

    private TypeVariance type;
    private Boolean appliquee;
    private LocalDateTime dateApplication;
}