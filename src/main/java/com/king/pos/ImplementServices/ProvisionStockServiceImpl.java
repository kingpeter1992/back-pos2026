package com.king.pos.ImplementServices;


import com.king.pos.Dao.StockRepository;
import com.king.pos.Dao.VenteLigneRepository;
import com.king.pos.Dto.Response.ProvisionStockDashboardResponse;
import com.king.pos.Dto.Response.ProvisionStockResponse;
import com.king.pos.Entitys.StockProduit;
import com.king.pos.Interface.ProvisionStockService;
import com.king.pos.enums.StatutVente;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProvisionStockServiceImpl implements ProvisionStockService {

    private final StockRepository stockProduitRepository;
    private final VenteLigneRepository venteLigneRepository;

    @Override
    public List<ProvisionStockResponse> calculerProvisionStock() {
        return stockProduitRepository.findAllDisponiblesAvecProduit()
                .stream()
                .map(this::mapToProvision)
                .sorted(Comparator.comparing(ProvisionStockResponse::getMontantProvisionFc).reversed())
                .toList();
    }

    @Override
    public ProvisionStockDashboardResponse getDashboardProvisionStock() {
        List<ProvisionStockResponse> lignes = calculerProvisionStock();

        BigDecimal valeurStockTotaleFc = lignes.stream()
                .map(ProvisionStockResponse::getValeurStockFc)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valeurStockTotaleUsd = lignes.stream()
                .map(ProvisionStockResponse::getValeurStockUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal provisionTotaleFc = lignes.stream()
                .map(ProvisionStockResponse::getMontantProvisionFc)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal provisionTotaleUsd = lignes.stream()
                .map(ProvisionStockResponse::getMontantProvisionUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long nombreProduits = lignes.size();

        long nombreProduitsProvisionnes = lignes.stream()
                .filter(l -> defaultIfNull(l.getMontantProvisionFc()).compareTo(BigDecimal.ZERO) > 0)
                .count();

        return ProvisionStockDashboardResponse.builder()
                .valeurStockTotale(valeurStockTotaleFc.setScale(2, RoundingMode.HALF_UP))
                .valeurStockTotaleFc(valeurStockTotaleFc.setScale(2, RoundingMode.HALF_UP))
                .valeurStockTotaleUsd(valeurStockTotaleUsd.setScale(2, RoundingMode.HALF_UP))
                .provisionTotale(provisionTotaleFc.setScale(2, RoundingMode.HALF_UP))
                .provisionTotaleFc(provisionTotaleFc.setScale(2, RoundingMode.HALF_UP))
                .provisionTotaleUsd(provisionTotaleUsd.setScale(2, RoundingMode.HALF_UP))
                .nombreProduits(nombreProduits)
                .nombreProduitsProvisionnes(nombreProduitsProvisionnes)
                .lignes(lignes)
                .build();
    }

    private ProvisionStockResponse mapToProvision(StockProduit stock) {
        Long produitId = stock.getProduit().getId();

        BigDecimal quantiteDisponible = defaultIfNull(stock.getQuantiteDisponible())
                .setScale(3, RoundingMode.HALF_UP);

        BigDecimal tauxChangeUtilise = defaultIfNull(stock.getTauxChangeUtilise())
                .setScale(6, RoundingMode.HALF_UP);

        BigDecimal pmp = defaultIfNull(stock.getPmp())
                .setScale(6, RoundingMode.HALF_UP);

        BigDecimal pmpFc = defaultIfNull(stock.getPmpFc())
                .setScale(6, RoundingMode.HALF_UP);

        BigDecimal pmpUsd = defaultIfNull(stock.getPmpUsd())
                .setScale(6, RoundingMode.HALF_UP);

        BigDecimal valeurStock = defaultIfNull(stock.getValeurStock())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal valeurStockFc = defaultIfNull(stock.getValeurStockFc());

        if (valeurStockFc.compareTo(BigDecimal.ZERO) == 0) {
            valeurStockFc = quantiteDisponible.multiply(pmpFc);
        }

        valeurStockFc = valeurStockFc.setScale(2, RoundingMode.HALF_UP);

        BigDecimal valeurStockUsd = defaultIfNull(stock.getValeurStockUsd());

        if (valeurStockUsd.compareTo(BigDecimal.ZERO) == 0) {
            if (pmpUsd.compareTo(BigDecimal.ZERO) > 0) {
                valeurStockUsd = quantiteDisponible.multiply(pmpUsd);
            } else if (tauxChangeUtilise.compareTo(BigDecimal.ZERO) > 0) {
                valeurStockUsd = valeurStockFc.divide(tauxChangeUtilise, 2, RoundingMode.HALF_UP);
            }
        }

        valeurStockUsd = valeurStockUsd.setScale(2, RoundingMode.HALF_UP);

        LocalDateTime derniereDateVente =
                venteLigneRepository.findDerniereDateVenteByProduitId(produitId, StatutVente.VALIDE);

        int joursSansVente = calculerJoursSansVente(derniereDateVente);

        BigDecimal tauxProvision = determinerTauxProvision(joursSansVente);

        BigDecimal montantProvisionFc = valeurStockFc
                .multiply(tauxProvision)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal montantProvisionUsd = valeurStockUsd
                .multiply(tauxProvision)
                .setScale(2, RoundingMode.HALF_UP);

        return ProvisionStockResponse.builder()
                .produitId(produitId)
                .codeBarres(stock.getProduit().getCodeBarres())
                .produitNom(stock.getProduit().getNom())
                .categorieNom(
                        stock.getProduit().getCategorie() != null
                                ? stock.getProduit().getCategorie().getNom()
                                : null
                )
                .quantiteDisponible(quantiteDisponible)
                .tauxChangeUtilise(tauxChangeUtilise)
                .pmp(pmp)
                .pmpFc(pmpFc)
                .pmpUsd(pmpUsd)
                .valeurStock(valeurStockFc)
                .valeurStockFc(valeurStockFc)
                .valeurStockUsd(valeurStockUsd)
                .joursSansVente(joursSansVente)
                .tauxProvision(tauxProvision)
                .montantProvision(montantProvisionFc)
                .montantProvisionFc(montantProvisionFc)
                .montantProvisionUsd(montantProvisionUsd)
                .niveauRisque(determinerNiveauRisque(tauxProvision))
                .build();
    }

    private int calculerJoursSansVente(LocalDateTime derniereDateVente) {
        if (derniereDateVente == null) {
            return 9999;
        }

        return (int) ChronoUnit.DAYS.between(
                derniereDateVente.toLocalDate(),
                LocalDate.now()
        );
    }

    private BigDecimal determinerTauxProvision(int joursSansVente) {
        if (joursSansVente >= 730) {
            return new BigDecimal("1.00");
        }

        if (joursSansVente >= 365) {
            return new BigDecimal("0.50");
        }

        if (joursSansVente >= 180) {
            return new BigDecimal("0.30");
        }

        return BigDecimal.ZERO;
    }

    private String determinerNiveauRisque(BigDecimal tauxProvision) {
        if (tauxProvision.compareTo(new BigDecimal("1.00")) == 0) {
            return "TOTAL";
        }

        if (tauxProvision.compareTo(new BigDecimal("0.50")) == 0) {
            return "ELEVE";
        }

        if (tauxProvision.compareTo(new BigDecimal("0.30")) == 0) {
            return "MOYEN";
        }

        return "FAIBLE";
    }

    private BigDecimal defaultIfNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}