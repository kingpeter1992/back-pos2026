package com.king.pos.Dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data @Setter @Getter
public class ProduitRequest {
 private String codeBarres;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    private String description;
    private Boolean actif;


    private Long categorieId;

    @NotNull(message = "Le prix de vente est obligatoire")
    private BigDecimal prixVente;

    @NotNull(message = "Le stock minimum est obligatoire")
    @Min(0)
    private Integer stockMinimum;

     @NotNull(message = "Le stock maximum est obligatoire")
    @Min(0)
    private Integer stockMaximum;

    private List<ImagePhotoRequest> images;

    private List<ProduitFournisseurRequest> fournisseurs;
}