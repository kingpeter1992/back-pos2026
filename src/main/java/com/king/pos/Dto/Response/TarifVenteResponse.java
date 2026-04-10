package com.king.pos.Dto.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TarifVenteResponse {
    private Long id;
    private String code;
    private String nom;
    private String description;
    private Boolean actif;
        private Boolean parDefaut;
    private LocalDate dateCreation;
}