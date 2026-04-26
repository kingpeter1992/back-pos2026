package com.king.pos.Dto.Response;

import lombok.Data;

@Data
public class DepotResponse {
    private Long id;
    private String nom;
    private String code;
    private String reference;
    private Boolean actif;
        private Boolean parDefaut;

}