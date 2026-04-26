package com.king.pos.Dto;


import lombok.Data;

@Data
public class InventaireBordereauCreateRequest {
    private Long locatorId;
    private String agentComptage;
    private String commentaire;
}
