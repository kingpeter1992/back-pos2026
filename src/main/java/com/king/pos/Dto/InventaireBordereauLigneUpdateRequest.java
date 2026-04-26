package com.king.pos.Dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventaireBordereauLigneUpdateRequest {
    private Long id;
    private BigDecimal quantiteComptee;
    private String commentaire;
    private String saisiPar;
}