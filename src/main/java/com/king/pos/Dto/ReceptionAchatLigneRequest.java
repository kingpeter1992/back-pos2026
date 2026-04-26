package com.king.pos.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;


@Data
public class ReceptionAchatLigneRequest {
     private Long produitId;
    private BigDecimal quantiteRecue;
    private BigDecimal prixAchatUnitaire;
    private LocalDate datePeremption;
    private String numeroLot;
}
