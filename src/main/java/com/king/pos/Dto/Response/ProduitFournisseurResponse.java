package com.king.pos.Dto.Response;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProduitFournisseurResponse {
    private Long id;
    private Long fournisseurId;
    private String fournisseurNom;
    private String referenceFournisseur;
    private BigDecimal prixAchat;
    private Integer delaiLivraisonJours;
    private Integer quantiteMinCommande;
    private Boolean fournisseurPrincipal;
    private Boolean actif;
}
