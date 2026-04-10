package com.king.pos.Dto;

import lombok.Data;

@Data
public class ReceptionLigneRequest {
    private Long ligneCommandeId;
    private Integer quantiteRecue;
}