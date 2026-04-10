package com.king.pos.Dto.Response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuggestionApprovisionnementResponse {
    private Long produitId;
    private String codeBarres;
    private String produit;
    private Integer stockActuel;
    private Integer stockMinimum;
    private Integer totalVendu30j;
    private Double moyenneJour;
    private Integer delaiLivraison;
    private String fournisseurPrincipal;
    private Integer quantiteACommander;
}