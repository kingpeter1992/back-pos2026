package com.king.pos.Utiltys;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.king.pos.ImplementServices.PeremptionService;

@Service
@RequiredArgsConstructor
public class PeremptionScheduler {

    private final PeremptionService peremptionService;

    @Scheduled(cron = "0 0 0 * * *")
    public void recalculerChaqueJour() {
        peremptionService.recalculerTousLesStatuts();
    }
}
