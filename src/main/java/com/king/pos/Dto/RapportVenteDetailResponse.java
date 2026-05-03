package com.king.pos.Dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RapportVenteDetailResponse {

    private String succursale;
    private String serviceCredite;
    private String module;
    private String natureOperation;

    private String numeroCC;
    private LocalDateTime dateCC;

    private String typeCommandeOuOR;
    private String libelleType;

    private String numeroClient;
    private String nomClient;

    private String codeRemise;
    private String tarif;
    private String operateur;

    private BigDecimal quantiteCommandee;
    private BigDecimal quantiteFacturee;

    private String userQuiALivre;

    private String numeroBL;
    private LocalDateTime dateBL;

    private String userQuiAFacture;

    private String numeroFacture;
    private LocalDateTime dateFacture;

    private Integer positionFacture;

    private String numeroBonCommande;
    private String libelleCommandeOuOR;

    private Integer numeroLigne;

    private String cst;
    private String reference;
    private String designation;

    private String codeRemiseLigne;
    private String codeGestion;
    private Integer geree;

    private BigDecimal coursDevise;

    private BigDecimal prixBrut;
    private BigDecimal remise;
    private BigDecimal prixNet;

    private BigDecimal pmp;

    private BigDecimal totalNet;
    private BigDecimal totalPmp;

    private BigDecimal marge;
    private BigDecimal pourcentageMarge;

    private BigDecimal tauxTva;
    private BigDecimal totalTtc;
}