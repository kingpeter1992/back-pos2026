package com.king.pos.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;


@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OuvrirCaisseDTO {

    @NotNull
    private double soldeInitialUSD = 0.0;

    @NotNull
    private double soldeInitialCDF = 0.0;

    private String note;
}