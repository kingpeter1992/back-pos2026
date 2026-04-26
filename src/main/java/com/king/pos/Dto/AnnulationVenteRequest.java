package com.king.pos.Dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnulationVenteRequest {

    @NotBlank(message = "Le commentaire d'annulation est obligatoire.")
    private String commentaire;
}