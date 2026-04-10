package com.king.pos.Dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LigneCommandeRequest {
    private Long produitId;
    private Integer quantite;
    private BigDecimal prixAchat;
}