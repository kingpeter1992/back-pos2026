package com.king.pos.ImplementServices;

import com.king.pos.Dao.StockLotRepository;
import com.king.pos.Entitys.StockLot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import com.king.pos.Utiltys.*;

@Service
@RequiredArgsConstructor
public class PeremptionService {

    private final StockLotRepository stockLotRepository;

    @Transactional
    public void recalculerTousLesStatuts() {
        List<StockLot> lots = stockLotRepository.findByQuantiteDisponibleGreaterThan(BigDecimal.ZERO);

        for (StockLot lot : lots) {
            lot.setStatutPeremption(
                    PeremptionUtils.calculerStatut(lot.getDatePeremption())
            );
        }

        stockLotRepository.saveAll(lots);
    }
}
