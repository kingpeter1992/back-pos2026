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
    private Integer stockMinimum;
    private Integer stockMaximum;
    private Boolean actif;
    private LocalDateTime dateCreation;
    private List<ImagePhotoResponse> images;
    private List<ProduitFournisseurResponse> fournisseurs;

}