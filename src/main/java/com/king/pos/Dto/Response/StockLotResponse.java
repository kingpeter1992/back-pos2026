package com.king.pos.Dto.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.king.pos.enums.StatutPeremption;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockLotResponse {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long produitId;
    private Long  depotId;
    private String produitNom;
    private String depotNom;
    private BigDecimal quantiteInitiale;
    private BigDecimal quantiteDisponible;
    private BigDecimal prixUnitaire;
    private BigDecimal fraisUnitaire;
    private BigDecimal coutUnitaireFinal;
    private LocalDate dateEntree;
    private LocalDate datePeremption;
    private StatutPeremption statutPeremption;
    private String referenceDocument;
    private String sourceDocument;
    private Long sourceDocumentId;
    private LocalDate dateCreation;
    private LocalDate dateModification;
    private String numeroLot;    
}
