package com.king.pos.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockAlerteView {
    private Long stockId;
    private Long produitId;
    private String nomProduit;
    private String codeBarre;
    private String categorie;
    private Long depotId;
    private String nomDepot;
    private BigDecimal quantiteDisponible;
    private Integer stockMinimum;
    private Integer stockMaximum;
    private String statutStock;
}