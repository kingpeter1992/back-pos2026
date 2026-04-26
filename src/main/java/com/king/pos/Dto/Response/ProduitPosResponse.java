package com.king.pos.Dto.Response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.*;

@Setter @Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProduitPosResponse {
  private Long id;
    private String nom;
    private String codeBarres;
    private String description;
    private BigDecimal prixVente;
    private BigDecimal stock;
    private BigDecimal stockSecurite;
    private BigDecimal pmp;
    private String imageUrl;
    private Boolean actif;
    private String categorie;
    private  BigDecimal prixAchat;
    private BigDecimal stockMinimum;
    private BigDecimal stockMaximum;
    private LocalDate dateCreation;
    private String fournisseurNom;
     
}