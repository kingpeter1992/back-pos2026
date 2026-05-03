package com.king.pos.Dto.Response;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProduitResponse {
   private Long id;
    private String codeBarres;
    private String nom;
    private String description;
    private Long categorieId;
    private String categorieNom;
    private Long fournisseurId;
    private String fournisseurNom;
    private BigDecimal prixAchat;
    private BigDecimal prixVente;
    private BigDecimal prixVenteFc;
    private BigDecimal prixVenteUsd;
    private BigDecimal tauxChangeUtilise;
    private BigDecimal stockMinimum;
    private BigDecimal stockMaximum;
    private Boolean actif;
    private String perissable;
    private LocalDateTime dateCreation;
    private List<ImagePhotoResponse> images;
    private List<ProduitFournisseurResponse> fournisseurs;

}