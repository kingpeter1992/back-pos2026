package com.king.pos.Dao;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.king.pos.enums.StatutPeremption;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertePeremptionResponse {
    private Long lotId;
    private Long produitId;
    private String produitNom;
    private String depotNom;
    private BigDecimal quantiteDisponible;
    private LocalDate datePeremption;
    private long joursRestants;
    private StatutPeremption statutPeremption;
    private String niveauAlerte;
    private String codeBarres;
    private Long depotId;
}