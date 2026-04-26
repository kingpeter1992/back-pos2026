package com.king.pos.Dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ReceptionLocatorLigneRequest {
     private Long produitId;
    private String locatorCode;
    private BigDecimal quantiteRangee;
}
