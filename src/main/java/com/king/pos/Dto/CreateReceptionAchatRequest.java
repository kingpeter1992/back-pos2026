package com.king.pos.Dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.king.pos.enums.Devise;

@Data
public class CreateReceptionAchatRequest {
    private Long commandeAchatId; // null si réception directe
    private Long fournisseurId;
    private Long depotId;

    private LocalDate dateReception;
    private String numeroLivraison;
    private String numeroFactureFournisseur;

    private Devise devise;
    private BigDecimal taux;

    private BigDecimal fraisTransport;
    private BigDecimal fraisDouane;
    private BigDecimal fraisManutention;
    private BigDecimal autresFrais;

    private String observation;

    private List<ReceptionAchatLigneRequest> lignes;
}
