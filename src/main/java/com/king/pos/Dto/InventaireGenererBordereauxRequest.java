package com.king.pos.Dto;

import lombok.Data;

@Data
public class InventaireGenererBordereauxRequest {
    private Integer tailleBordereau; // ex: 50
    private Boolean afficherQuantiteTheorique;
    private String creePar;
}