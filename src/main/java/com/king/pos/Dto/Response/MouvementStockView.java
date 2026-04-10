package com.king.pos.Dto.Response;

import com.king.pos.enums.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MouvementStockView {

    private Long id;
    private LocalDateTime dateMouvement;
    private com.king.pos.enums.TypeMouvementStock typeMouvement;

    private Long produitId;
    private String nomProduit;
    private String codeBarres;

    private Long depotId;
    private String nomDepot;

    private BigDecimal quantite;

    private BigDecimal prixUnitaireEntree;
    private BigDecimal fraisUnitaire;
    private BigDecimal coutUnitaireFinal;

    private BigDecimal ancienStock;
    private BigDecimal ancienPmp;

    private BigDecimal nouveauStock;
    private BigDecimal nouveauPmp;

    private String referenceDocument;
    private String libelle;
}
