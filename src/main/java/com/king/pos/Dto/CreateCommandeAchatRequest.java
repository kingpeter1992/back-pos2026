package com.king.pos.Dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.king.pos.enums.Devise;

@Data
public class CreateCommandeAchatRequest {
    private Long fournisseurId;
    private String prefixe;
    private String refCommande;
    private BigDecimal montantBrut;
    private BigDecimal montantRemise;
    private BigDecimal montantTotal;
    private Devise devise;
    private BigDecimal taux;
    private LocalDate dateCommande;
    private LocalDate dateLivraisonPrevue;
    private String observation;
    private List<CommandeAchatLigneRequest> lignes;
}