package com.king.pos.Dto;

import java.time.LocalDate;

public record SessionReportDTO(
        Long sessionId,
        LocalDate dateJour,
        String statut,
        double encUSD,
        double decUSD,
        double encCDF,
        double decCDF,
        double soldeDebutUSD,
        double soldeFinUSD,
        double soldeDebutCDF,
        double soldeFinCDF,
        long nbOperations
) {}