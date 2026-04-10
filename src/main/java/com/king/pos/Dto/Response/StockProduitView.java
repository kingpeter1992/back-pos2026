package com.king.pos.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockProduitView {private Long stockId;
    private Long produitId;
    private String nomProduit;
    private String codeBarre;
    private String categorie;
    private Long depotId;
    private String nomDepot;

    private BigDecimal quantiteDisponible;
    private BigDecimal pmp;
    private BigDecimal valeurStock;

    private Integer stockMinimum;
    private Integer stockMaximum;

    private String statutStock;

    private LocalDateTime dateDerniereEntree;
    private LocalDateTime dateDerniereSortie;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
