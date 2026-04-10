package com.king.pos.Dto;

import com.king.pos.Entitys.Categorie;
import com.king.pos.Entitys.ModePaiement;
import com.king.pos.Entitys.TypeTransaction;
import com.king.pos.enums.Devise;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OperationCaisseDTO {

    @NotNull
    private TypeTransaction type; // ENCAISSEMENT / DECAISSEMENT

    @NotNull
    private Devise devise; // USD / CDF

    @NotNull
    private double montant;

    @NotNull
    private Categorie category; // SALAIRE, AVANCE, FACTURE, REMBOURSEMENT, AUTRE...

    @NotNull
    private ModePaiement modePaiement; // CASH, MOBILE_MONEY, BANQUE

    private String description;

    /**
     * ex: FACT-12, DEP-171..., REM-..., SAL-...
     * (facultatif si tu génères côté service)
     */
    private String reference;

    /** L'un ou l'autre (ou aucun) */
    private Long clientId;
    private Long gardienId;

    /**
     * Optionnel: si tu veux lier à une facture
     * (utile pour encaissement client facture)
     */
    private Long factureId;
}
