package com.king.pos.ImplementServices;


import com.king.pos.Dao.StockLotRepository;
import com.king.pos.Dto.LotConsommationResult;
import com.king.pos.Entitys.Depot;
import com.king.pos.Entitys.Produit;
import com.king.pos.Entitys.StockLot;
import com.king.pos.Entitys.VenteLotConsommation;
import com.king.pos.Handllers.BusinessException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.king.pos.Utiltys.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SortieStockLotService {

    private final StockLotRepository stockLotRepository;

    @Transactional
    public List<LotConsommationResult> consommerEnFefo(
            Produit produit,
            Depot depot,
            BigDecimal quantiteDemandee
    ) {
        BigDecimal resteAConsommer = scale3(quantiteDemandee);

        List<StockLot> lots = stockLotRepository.findLotsDisponiblesPourSortie(
                produit.getId(),
                depot.getId()
        );

        if (lots.isEmpty()) {
            throw new BusinessException("Aucun lot disponible pour le produit : " + produit.getNom());
        }

        List<LotConsommationResult> resultats = new ArrayList<>();

        for (StockLot lot : lots) {
            lot.setStatutPeremption(PeremptionUtils.calculerStatut(lot.getDatePeremption()));

            if (PeremptionUtils.estBloquantPourVente(lot.getStatutPeremption())) {
                stockLotRepository.save(lot);
                continue;
            }

            if (resteAConsommer.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal disponible = scale3(lot.getQuantiteDisponible());
            if (disponible.compareTo(BigDecimal.ZERO) <= 0) {
                stockLotRepository.save(lot);
                continue;
            }

            BigDecimal quantitePrise = disponible.min(resteAConsommer);
            BigDecimal nouveauDisponible = scale3(disponible.subtract(quantitePrise));

            lot.setQuantiteDisponible(nouveauDisponible);
            lot.setStatutPeremption(PeremptionUtils.calculerStatut(lot.getDatePeremption()));
            stockLotRepository.save(lot);

            resultats.add(
                    LotConsommationResult.builder()
                            .stockLot(lot)
                            .quantiteConsommee(quantitePrise)
                            .quantiteRestanteLot(nouveauDisponible)
                            .coutUnitaireFinal(nvl(lot.getCoutUnitaireFinal()))
                            .datePeremption(lot.getDatePeremption())
                            .build()
            );

            resteAConsommer = scale3(resteAConsommer.subtract(quantitePrise));
        }

        if (resteAConsommer.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException(
                    "Stock insuffisant en lots valides pour le produit : " + produit.getNom()
            );
        }

        return resultats;
    }

    @Transactional
    public void remettreEnStockLotsAnnules(List<VenteLotConsommation> consommations) {
        for (VenteLotConsommation consommation : consommations) {
            StockLot lot = consommation.getStockLot();

            BigDecimal ancienneQte = scale3(lot.getQuantiteDisponible());
            BigDecimal qteARemettre = scale3(consommation.getQuantiteConsommee());

            lot.setQuantiteDisponible(scale3(ancienneQte.add(qteARemettre)));
            lot.setStatutPeremption(PeremptionUtils.calculerStatut(lot.getDatePeremption()));

            stockLotRepository.save(lot);
        }
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal scale3(BigDecimal value) {
        return nvl(value).setScale(3, RoundingMode.HALF_UP);
    }
}
