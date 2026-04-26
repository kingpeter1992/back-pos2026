package com.king.pos.Utiltys;


import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.king.pos.enums.StatutPeremption;

public final class PeremptionUtils {

    private PeremptionUtils() {
    }

    public static long calculerJoursRestants(LocalDate datePeremption) {
        if (datePeremption == null) {
            return Long.MAX_VALUE;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), datePeremption);
    }

    public static StatutPeremption calculerStatut(LocalDate datePeremption) {
        if (datePeremption == null) {
            return StatutPeremption.VALIDE;
        }

        long joursRestants = calculerJoursRestants(datePeremption);

        if (joursRestants < 0) {
            return StatutPeremption.PERIME;
        }
        if (joursRestants == 0) {
            return StatutPeremption.EXPIRE_AUJOURD_HUI;
        }
        if (joursRestants <= 7) {
            return StatutPeremption.ALERTE_7_JOURS;
        }
        if (joursRestants <= 30) {
            return StatutPeremption.ALERTE_30_JOURS;
        }
        if (joursRestants <= 170) {
            return StatutPeremption.ALERTE_170_JOURS;
        }
        if (joursRestants <= 350) {
            return StatutPeremption.ALERTE_350_JOURS;
        }

        return StatutPeremption.VALIDE;
    }

    public static boolean estBloquantPourVente(StatutPeremption statut) {
         return statut == StatutPeremption.PERIME
        || statut == StatutPeremption.EXPIRE_AUJOURD_HUI;
    }

    public static boolean estEnAlerte(StatutPeremption statut) {
        return statut != null && statut != StatutPeremption.VALIDE;
    }
}