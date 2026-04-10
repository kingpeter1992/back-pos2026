package com.king.pos.Dto;



import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProduitFournisseurRequest {
    private Long fournisseurId;
    private String referenceFournisseur;
    private BigDecimal prixAchat;
    private Integer delaiLivraisonJours;
    private Integer quantiteMinCommande;
    private Boolean fournisseurPrincipal;
}