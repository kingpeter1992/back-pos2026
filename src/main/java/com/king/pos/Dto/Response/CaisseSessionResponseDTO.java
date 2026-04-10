package com.king.pos.Dto.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CaisseSessionResponseDTO {

    private Long id;
    private LocalDate dateJour;
    private String statut;

    // Soldes ouverture
    private double soldeInitialUSD;
    private double soldeInitialCDF;

    // Soldes actuels
    private double soldeActuelUSD;
    private double soldeActuelCDF;

    // Dates
    private LocalDateTime dateOuverture;
    private LocalDateTime dateCloture;

    private String openedBy;
    private String closedBy;

    private String noteOuverture;
    private String noteCloture;

    // Infos calculées
    private int totalTransactions;
}