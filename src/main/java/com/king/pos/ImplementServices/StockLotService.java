package com.king.pos.ImplementServices;


import com.king.pos.Dao.StockLotRepository;
import com.king.pos.Dto.Response.StockLotResponse;
import com.king.pos.Entitys.Depot;
import com.king.pos.Entitys.Produit;
import com.king.pos.Entitys.StockLot;
import com.king.pos.enums.StatutPeremption;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.king.pos.Utiltys.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockLotService {

    private final StockLotRepository stockLotRepository;

    @Transactional
public StockLot creerLotEntree(StockLot stockLot) {

    StatutPeremption statut =
            PeremptionUtils.calculerStatut(stockLot.getDatePeremption());

    StockLot lot = StockLot.builder()
            .produit(stockLot.getProduit())
            .depot(stockLot.getDepot())

            .quantiteInitiale(scale3(stockLot.getQuantiteInitiale()))
            .quantiteDisponible(scale3(stockLot.getQuantiteDisponible()))

            .prixUnitaire(scale6(stockLot.getPrixUnitaire()))
            .fraisUnitaire(scale6(stockLot.getFraisUnitaire()))
            .coutUnitaireFinal(scale6(stockLot.getCoutUnitaireFinal()))

            // ===== MULTIDEVISE =====
            .tauxChangeUtilise(scale6(stockLot.getTauxChangeUtilise()))

            .prixUnitaireFc(scale6(stockLot.getPrixUnitaireFc()))
            .prixUnitaireUsd(scale6(stockLot.getPrixUnitaireUsd()))

            .fraisUnitaireFc(scale6(stockLot.getFraisUnitaireFc()))
            .fraisUnitaireUsd(scale6(stockLot.getFraisUnitaireUsd()))

            .coutUnitaireFinalFc(scale6(stockLot.getCoutUnitaireFinalFc()))
            .coutUnitaireFinalUsd(scale6(stockLot.getCoutUnitaireFinalUsd()))

            .montantLigneFc(scale2(stockLot.getMontantLigneFc()))
            .montantLigneUsd(scale2(stockLot.getMontantLigneUsd()))

            // ===== LOT =====
            .dateEntree(
                    stockLot.getDateEntree() != null
                            ? stockLot.getDateEntree()
                            : LocalDate.now()
            )
            .datePeremption(stockLot.getDatePeremption())
            .numeroLot(stockLot.getNumeroLot())

            .statutPeremption(statut)

            .referenceDocument(stockLot.getReferenceDocument())
            .sourceDocument(stockLot.getSourceDocument())
            .sourceDocumentId(stockLot.getSourceDocumentId())

            .build();

    return stockLotRepository.save(lot);
}

private BigDecimal scale2(BigDecimal value) {
    return nvl(value).setScale(2, RoundingMode.HALF_UP);
}
    @Transactional
    public void mettreAJourStatutLot(StockLot lot) {
        lot.setStatutPeremption(
                PeremptionUtils.calculerStatut(lot.getDatePeremption())
        );
        stockLotRepository.save(lot);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal scale3(BigDecimal value) {
        return nvl(value).setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal scale6(BigDecimal value) {
        return nvl(value).setScale(6, RoundingMode.HALF_UP);
    }

@Transactional(readOnly = true)
public List<StockLotResponse> getAll() {

    List<StockLot> lots = stockLotRepository.findAll();

    if (log.isDebugEnabled()) {
        lots.forEach(stockLot -> {
            log.debug("========== LOT ==========");
            log.debug("Lot ID: {}", stockLot.getId());

            if (stockLot.getProduit() != null) {
                log.debug("Produit ID: {}", stockLot.getProduit().getId());
                log.debug("Produit Nom: {}", stockLot.getProduit().getNom());
            } else {
                log.warn("Produit NULL pour le lot ID: {}", stockLot.getId());
            }

            log.debug("Date Péremption: {}", stockLot.getDatePeremption());
            log.debug("Date Entrée: {}", stockLot.getDateEntree());
            log.debug("Quantité Disponible: {}", stockLot.getQuantiteDisponible());
            log.debug("Statut Péremption: {}", stockLot.getStatutPeremption());
            System.out.println("Date entrée : " + stockLot.getDateEntree());
            System.out.println("Date péremption : " + stockLot.getDatePeremption());
            System.out.println("Quantité disponible : " + stockLot.getQuantiteDisponible());
            System.out.println("Statut péremption : " + stockLot.getStatutPeremption());
            System.out.println("==========================");
           
        });
    }

    return lots.stream()
            .map(this::mapToResponse)
            .toList();
}


private StockLotResponse mapToResponse(StockLot lot) {
    return StockLotResponse.builder()
            .id(lot.getId())
            .produitId(lot.getProduit() != null ? lot.getProduit().getId() : null)
            .produitNom(lot.getProduit() != null ? lot.getProduit().getNom() : null)
            .depotId(lot.getDepot() != null ? lot.getDepot().getId() : null)
            .depotNom(lot.getDepot() != null ? lot.getDepot().getNom() : null)
           .quantiteInitiale(lot.getQuantiteInitiale())
            .quantiteDisponible(lot.getQuantiteDisponible())

            .prixUnitaire(lot.getPrixUnitaire())
            .fraisUnitaire(lot.getFraisUnitaire())
            .coutUnitaireFinal(lot.getCoutUnitaireFinal())

            // MULTIDEVISE
            .tauxChangeUtilise(lot.getTauxChangeUtilise())

            .prixUnitaireFc(lot.getPrixUnitaireFc())
            .prixUnitaireUsd(lot.getPrixUnitaireUsd())

            .fraisUnitaireFc(lot.getFraisUnitaireFc())
            .fraisUnitaireUsd(lot.getFraisUnitaireUsd())

            .coutUnitaireFinalFc(lot.getCoutUnitaireFinalFc())
            .coutUnitaireFinalUsd(lot.getCoutUnitaireFinalUsd())

            .montantLigneFc(lot.getMontantLigneFc())
            .montantLigneUsd(lot.getMontantLigneUsd())

            .dateEntree(lot.getDateEntree())
            .datePeremption(lot.getDatePeremption())

            .statutPeremption(lot.getStatutPeremption())

            .referenceDocument(lot.getReferenceDocument())
            .sourceDocument(lot.getSourceDocument())
            .sourceDocumentId(lot.getSourceDocumentId())

            .dateCreation(lot.getDateCreation())
            .dateModification(lot.getDateModification())

            .numeroLot(lot.getNumeroLot())
            .build();
}
}
