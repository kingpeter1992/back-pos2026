package com.king.pos.ImplementServices;

import com.king.pos.enums.TypeMouvementStock;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class StockAjustementInventaireService {

    private final TransactionStockService transactionStockService;

    /**
     * Applique un ajustement de stock issu d'un inventaire.
     *
     * Règle :
     * - ecart > 0  => entrée de stock
     * - ecart < 0  => sortie de stock
     * - ecart = 0  => aucun mouvement
     */
    @Transactional
    public void ajusterDepuisInventaire(
            Long produitId,
            Long depotId,
            Long locatorId,
            Long stockLotId,
            BigDecimal ecart,
            BigDecimal pmp,
            String referenceInventaire
    ) {
        validateIds(produitId, depotId);

        if (ecart == null || ecart.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        BigDecimal quantite = ecart.abs();
        BigDecimal pmpValue = nvl(pmp);

        if (ecart.compareTo(BigDecimal.ZERO) > 0) {
            transactionStockService.entree(
                    produitId,
                    depotId,
                    locatorId,
                    stockLotId,
                    quantite,
                    pmpValue,
                    TypeMouvementStock.AJUSTEMENT_INVENTAIRE_ENTREE,
                    "INVENTAIRE",
                    referenceInventaire
            );
        } else {
            transactionStockService.sortie(
                    produitId,
                    depotId,
                    locatorId,
                    stockLotId,
                    quantite,
                    pmpValue,
                    TypeMouvementStock.AJUSTEMENT_INVENTAIRE_SORTIE,
                    "INVENTAIRE",
                    referenceInventaire
            );
        }
    }

    /**
     * Annule tous les ajustements de stock déjà appliqués
     * pour une référence d'inventaire donnée.
     *
     * L'inversion réelle des mouvements est déléguée à TransactionStockService.
     */
    @Transactional
    public void annulerAjustementsDepuisReference(String referenceInventaire) {
        if (referenceInventaire == null || referenceInventaire.isBlank()) {
            return;
        }

        transactionStockService.annulerParReference(referenceInventaire, "INVENTAIRE");
    }

    private void validateIds(Long produitId, Long depotId) {
        if (produitId == null) {
            throw new IllegalArgumentException("produitId est obligatoire.");
        }

        if (depotId == null) {
            throw new IllegalArgumentException("depotId est obligatoire.");
        }
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}