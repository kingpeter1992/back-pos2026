package com.king.pos.Dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;


@Data
@Builder
public class InventaireResultatLigneResponse {

    private String depot;
    private String numeroInventaire;
    private String numeroBordereau;

    private String locator;
    private String statutBordereau;

    private LocalDateTime dateInventaire;
    private LocalDateTime dateMiseAJourStock;

    private Integer numeroLigne;

    private String codeArticle;
    private String designation;

    private BigDecimal quantiteStockTheorique;
    private BigDecimal quantiteComptee;
    private BigDecimal quantiteEcart;

    private Boolean ecart;

    private BigDecimal pmpInventaire;

    private BigDecimal valeurStockTheorique;
    private BigDecimal valeurStockComptee;
    private BigDecimal valeurEcart;

    private String typeVariance;

    private Boolean stockMisAJour;

    private BigDecimal stockActuel;
    private BigDecimal pmpActuel;
    private BigDecimal valeurStockActuel;

    private String commentaireComptage;
}