package com.king.pos.Dto.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class ReceptionAchatResponse {
    private Long id;
    private String refReception;
    private LocalDate dateReception;
    private String statut;

    private Long depotId;
    private String depotNom;

    private Long fournisseurId;
    private String fournisseurNom;

    private Long commandeAchatId;
    private String refCommande;

    private BigDecimal totalMarchandise;
    private BigDecimal totalFrais;
    private BigDecimal totalGeneral;

    private List<ReceptionAchatLigneResponse> lignes;
}