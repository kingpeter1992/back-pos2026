package com.king.pos.Dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RapportVenteFilterRequest {

    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    private Long depotId;
    private Long categorieId;
    private Long tarifId;

    private String caissier;
    private String devise;
}
