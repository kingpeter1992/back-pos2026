package com.king.pos.Dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CaisseSessionDto {

    private Long id;
    private LocalDate dateJour;
    private String statut;

    private LocalDateTime dateOuverture;
    private LocalDateTime dateCloture;

    private double soldeInitialUSD;
    private double soldeInitialCDF;

    private double soldeActuelUSD;
    private double soldeActuelCDF;

    private String openedBy;
    private String closedBy;

    private String noteOuverture;
    private String noteCloture;
}