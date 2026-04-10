package com.king.pos.Dto;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class CommandeAchatLigneRequest {
 private Long id;
    private Long produitId;
    private BigDecimal quantite;
    private BigDecimal prixUnitaire;
     private BigDecimal remise;
     
}