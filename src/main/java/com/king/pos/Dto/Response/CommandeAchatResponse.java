package com.king.pos.Dto.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.king.pos.enums.Devise;
import com.king.pos.enums.StatutCommandeFournisseur;
import com.king.pos.enums.StatutReceptionAchat;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class CommandeAchatResponse {
    private Long id;
 private Long fournisseurId;
    private String prefixe;
    private String refCommande;
    private StatutCommandeFournisseur statut;
    private BigDecimal montantBrut;
    private BigDecimal montantRemise;
    private BigDecimal montantTotal;
    private Devise devise;
    private BigDecimal taux;

        // NOUVEAU
    private BigDecimal montantTotalFc;
    private BigDecimal montantTotalUsd;
    private BigDecimal tauxChangeUtilise;

    
    private LocalDate dateCommande;
    private LocalDate datePrevue;
    private String fournisseurNom;
    private LocalDate dateLivraisonPrevue;
    private String observation;
    private StatutReceptionAchat positionLivraison;
    private String user;

    private List<CommandeAchatLigneResponse> lignes;
}
