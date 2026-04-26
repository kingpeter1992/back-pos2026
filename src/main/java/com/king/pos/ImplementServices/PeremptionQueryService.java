package com.king.pos.ImplementServices;


import com.king.pos.Dao.AlertePeremptionResponse;
import com.king.pos.Dao.StockLotRepository;
import com.king.pos.Dto.Response.DashboardPeremptionResponse;
import com.king.pos.Entitys.StockLot;
import com.king.pos.Utiltys.PeremptionUtils;
import com.king.pos.enums.StatutPeremption;
import lombok.extern.slf4j.Slf4j;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PeremptionQueryService {

    private final StockLotRepository stockLotRepository;


public List<AlertePeremptionResponse> getAlertes() {
    List<StockLot> lots = stockLotRepository.findLotsEnAlerte();

    return lots.stream()
            .map(this::mapToAlerteResponse)
            .sorted(Comparator.comparing(
                    AlertePeremptionResponse::getDatePeremption,
                    Comparator.nullsLast(Comparator.naturalOrder())
            ))
            .toList();
}


    public DashboardPeremptionResponse getDashboard() {
        BigDecimal zero = BigDecimal.ZERO;

        long totalLots = stockLotRepository.countByQuantiteDisponibleGreaterThan(zero);
        long totalLotsValides = stockLotRepository.countByQuantiteDisponibleGreaterThanAndStatutPeremption(zero, StatutPeremption.VALIDE);
        long totalLotsPerimes = stockLotRepository.countByQuantiteDisponibleGreaterThanAndStatutPeremption(zero, StatutPeremption.PERIME);
        long totalExpireAujourdHui = stockLotRepository.countByQuantiteDisponibleGreaterThanAndStatutPeremption(zero, StatutPeremption.EXPIRE_AUJOURD_HUI);
        long totalAlerte7Jours = stockLotRepository.countByQuantiteDisponibleGreaterThanAndStatutPeremption(zero, StatutPeremption.ALERTE_7_JOURS);
        long totalAlerte30Jours = stockLotRepository.countByQuantiteDisponibleGreaterThanAndStatutPeremption(zero, StatutPeremption.ALERTE_30_JOURS);
        long totalAlerte170Jours = stockLotRepository.countByQuantiteDisponibleGreaterThanAndStatutPeremption(zero, StatutPeremption.ALERTE_170_JOURS);
        long totalAlerte350Jours = stockLotRepository.countByQuantiteDisponibleGreaterThanAndStatutPeremption(zero, StatutPeremption.ALERTE_350_JOURS);

        long totalLotsEnAlerte =
                totalLotsPerimes
                        + totalExpireAujourdHui
                        + totalAlerte7Jours
                        + totalAlerte30Jours
                        + totalAlerte170Jours
                        + totalAlerte350Jours;

        return DashboardPeremptionResponse.builder()
                .totalLots(totalLots)
                .totalLotsValides(totalLotsValides)
                .totalLotsEnAlerte(totalLotsEnAlerte)
                .totalLotsPerimes(totalLotsPerimes)
                .totalExpireAujourdHui(totalExpireAujourdHui)
                .totalAlerte7Jours(totalAlerte7Jours)
                .totalAlerte30Jours(totalAlerte30Jours)
                .totalAlerte170Jours(totalAlerte170Jours)
                .totalAlerte350Jours(totalAlerte350Jours)
                .build();
    }

    private AlertePeremptionResponse mapToAlerteResponse(StockLot lot) {
        return AlertePeremptionResponse.builder()
                .lotId(lot.getId())
                .produitId(lot.getProduit() != null ? lot.getProduit().getId() : null)
                .produitNom(lot.getProduit() != null ? lot.getProduit().getNom() : null)
                .codeBarres(lot.getProduit() != null ? lot.getProduit().getCodeBarres() : null)
                .depotId(lot.getDepot() != null ? lot.getDepot().getId() : null)
                .depotNom(lot.getDepot() != null ? lot.getDepot().getNom() : null)
                .quantiteDisponible(lot.getQuantiteDisponible())
                .datePeremption(lot.getDatePeremption())
                .joursRestants(PeremptionUtils.calculerJoursRestants(lot.getDatePeremption()))
                .statutPeremption(lot.getStatutPeremption())
                .niveauAlerte(getNiveauAlerte(lot.getStatutPeremption()))
                .build();
    }

    private String getNiveauAlerte(StatutPeremption statut) {
        return switch (statut) {
            case PERIME, EXPIRE_AUJOURD_HUI -> "CRITIQUE";
            case ALERTE_7_JOURS, ALERTE_30_JOURS -> "ELEVEE";
            case ALERTE_170_JOURS, ALERTE_350_JOURS -> "SURVEILLANCE";
            default -> "OK";
        };
    }
}