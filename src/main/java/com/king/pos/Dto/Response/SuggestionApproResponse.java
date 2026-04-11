package com.king.pos.Dto.Response;


import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestionApproResponse {


    private Long produitId;
    private String produitNom;
    private String codeBarres;
    private String categorieNom;

    private BigDecimal stockActuel;
    private BigDecimal quantiteVendue30Jours;
    private BigDecimal rotationJournaliere;

    private Integer delaiApprovisionnementJours;
    private Integer joursSecurite;
    private Integer couvertureCibleJours;

    private BigDecimal stockSecurite;
    private BigDecimal pointCommande;
    private BigDecimal stockCible;
    private BigDecimal quantiteACommander;

    private String statutAppro;
    private Integer joursCouvertureRestants;
}