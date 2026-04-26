package com.king.pos.Dto;


import lombok.Data;

import java.time.LocalDate;

import com.king.pos.enums.TypeInventaire;

@Data
public class InventaireCreateRequest {
    private TypeInventaire type;
    private Long depotId;
    private Long locatorId;
    private LocalDate dateInventaire;
    private String commentaire;
    private Boolean memorise;
    private Boolean gelStockTheorique;
    private String creePar;
}