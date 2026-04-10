package com.king.pos.Dto;

import lombok.Data;

@Data
public class TarifVenteRequest {
    private String code;
    private String nom;
    private String description;
    private Boolean actif;
    private Boolean parDefaut;
}