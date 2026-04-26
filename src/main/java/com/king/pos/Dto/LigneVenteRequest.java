package com.king.pos.Dto;

import java.math.BigDecimal;

import lombok.*;

@Getter
@Setter
public class LigneVenteRequest {
    private Long produitId;
    private BigDecimal quantite;
    private BigDecimal prix;
    private BigDecimal remise;
    private BigDecimal total;
}
