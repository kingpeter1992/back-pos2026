package com.king.pos.ImplementServices;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.king.pos.Dao.DepotRepository;
import com.king.pos.Dao.ProduitRepository;
import com.king.pos.Dao.StockRepository;
import com.king.pos.Dao.TransactionStockRepository;
import com.king.pos.Dto.TransactionStockRequest;
import com.king.pos.Entitys.Depot;
import com.king.pos.Entitys.Produit;
import com.king.pos.Entitys.StockProduit;
import com.king.pos.Entitys.TransactionStock;
import com.king.pos.Handllers.BusinessException;
import com.king.pos.enums.TypeMouvementStock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionStockService {

    private final StockRepository stockProduitRepository;
    private final TransactionStockRepository transactionStockRepository;
    private final ProduitRepository produitRepository;
    private final DepotRepository depotRepository;

    @Transactional
    public void appliquerTransaction(TransactionStockRequest request) {
        if (request.getProduit() == null) {
            throw new BusinessException("Le produit est obligatoire.");
        }

        if (request.getDepot() == null) {
            throw new BusinessException("Le dépôt est obligatoire.");
        }

        BigDecimal quantite = scale3(nvl(request.getQuantite()));
        if (quantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La quantité doit être supérieure à zéro.");
        }

        StockProduit stock = stockProduitRepository.findByProduitAndDepot(request.getProduit(), request.getDepot())
                .orElseGet(() -> stockProduitRepository.save(
                        StockProduit.builder()
                                .produit(request.getProduit())
                                .depot(request.getDepot())
                                .quantiteDisponible(BigDecimal.ZERO)
                                .pmp(BigDecimal.ZERO)
                                .valeurStock(BigDecimal.ZERO)
                                .dateCreation(LocalDateTime.now())
                                .build()
                ));

        BigDecimal stockAvant = scale3(nvl(stock.getQuantiteDisponible()));
        BigDecimal pmpAvant = scale6(nvl(stock.getPmp()));

        BigDecimal stockApres;
        BigDecimal pmpApres = pmpAvant;

        boolean entree = isEntree(request.getTypeTransaction());

        if (entree) {
            stockApres = scale3(stockAvant.add(quantite));

            BigDecimal coutFinal = scale6(nvl(request.getCoutUnitaireFinal()));
            BigDecimal valeurAvant = stockAvant.multiply(pmpAvant);
            BigDecimal valeurEntree = quantite.multiply(coutFinal);

            pmpApres = stockApres.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : valeurAvant.add(valeurEntree).divide(stockApres, 6, RoundingMode.HALF_UP);

        } else {
            if (stockAvant.compareTo(quantite) < 0) {
                throw new BusinessException(
                        "Stock insuffisant pour le produit : " + request.getProduit().getNom()
                );
            }

            stockApres = scale3(stockAvant.subtract(quantite));

            if (stockApres.compareTo(BigDecimal.ZERO) == 0) {
                pmpApres = BigDecimal.ZERO;
            }
        }

        stock.setQuantiteDisponible(stockApres);
        stock.setPmp(scale6(pmpApres));
        stock.setValeurStock(scale2(stockApres.multiply(pmpApres)));
        stock.setDateDerniereMiseAJour(LocalDateTime.now());

        stockProduitRepository.save(stock);

        TransactionStock transaction = TransactionStock.builder()
                .dateTransaction(LocalDateTime.now())
                .typeMouvement(request.getTypeTransaction())
                .produit(request.getProduit())
                .depot(request.getDepot())
                .quantite(quantite)
                .stockAvant(stockAvant)
                .stockApres(stockApres)
                .pmpAvant(scale6(pmpAvant))
                .pmpApres(scale6(pmpApres))
                .prixUnitaire(scale6(nvl(request.getPrixUnitaire())))
                .fraisUnitaire(scale6(nvl(request.getFraisUnitaire())))
                .coutUnitaireFinal(scale6(nvl(request.getCoutUnitaireFinal())))
                .referenceDocument(request.getReferenceDocument())
                .sourceDocument(request.getSourceDocument())
                .sourceDocumentId(request.getSourceDocumentId())
                .libelle(request.getLibelle())
                .utilisateur(request.getUtilisateur())
                .build();

        transactionStockRepository.save(transaction);
    }

    /**
     * Entrée stock standard, utilisée par l'inventaire et autres mouvements.
     *
     * locatorId et stockLotId sont acceptés pour compatibilité avec les appels
     * métier, mais ne sont pas encore persistés dans TransactionStock.
     */
    @Transactional
    public void entree(
            Long produitId,
            Long depotId,
            Long locatorId,
            Long stockLotId,
            BigDecimal quantite,
            BigDecimal coutUnitaireFinal,
            TypeMouvementStock typeMouvement,
            String sourceDocument,
            String referenceDocument
    ) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new BusinessException("Produit introuvable"));

        Depot depot = depotRepository.findById(depotId)
                .orElseThrow(() -> new BusinessException("Dépôt introuvable"));

        TransactionStockRequest request = new TransactionStockRequest();
        request.setProduit(produit);
        request.setDepot(depot);
        request.setQuantite(scale3(nvl(quantite)));
        request.setTypeTransaction(typeMouvement);
        request.setPrixUnitaire(scale6(nvl(coutUnitaireFinal)));
        request.setFraisUnitaire(BigDecimal.ZERO);
        request.setCoutUnitaireFinal(scale6(nvl(coutUnitaireFinal)));
        request.setReferenceDocument(referenceDocument);
        request.setSourceDocument(sourceDocument);
        request.setSourceDocumentId(null);
        request.setLibelle("Entrée stock - " + typeMouvement.name());
        request.setUtilisateur("SYSTEM");

        appliquerTransaction(request);
    }

    /**
     * Sortie stock standard, utilisée par l'inventaire et autres mouvements.
     *
     * locatorId et stockLotId sont acceptés pour compatibilité avec les appels
     * métier, mais ne sont pas encore persistés dans TransactionStock.
     */
    @Transactional
    public void sortie(
            Long produitId,
            Long depotId,
            Long locatorId,
            Long stockLotId,
            BigDecimal quantite,
            BigDecimal coutUnitaireFinal,
            TypeMouvementStock typeMouvement,
            String sourceDocument,
            String referenceDocument
    ) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new BusinessException("Produit introuvable"));

        Depot depot = depotRepository.findById(depotId)
                .orElseThrow(() -> new BusinessException("Dépôt introuvable"));

        TransactionStockRequest request = new TransactionStockRequest();
        request.setProduit(produit);
        request.setDepot(depot);
        request.setQuantite(scale3(nvl(quantite)));
        request.setTypeTransaction(typeMouvement);
        request.setPrixUnitaire(scale6(nvl(coutUnitaireFinal)));
        request.setFraisUnitaire(BigDecimal.ZERO);
        request.setCoutUnitaireFinal(scale6(nvl(coutUnitaireFinal)));
        request.setReferenceDocument(referenceDocument);
        request.setSourceDocument(sourceDocument);
        request.setSourceDocumentId(null);
        request.setLibelle("Sortie stock - " + typeMouvement.name());
        request.setUtilisateur("SYSTEM");

        appliquerTransaction(request);
    }

    /**
     * Annule tous les mouvements d'une référence donnée.
     *
     * Exemple :
     * - si un ajustement inventaire a fait une entrée, on fait une sortie inverse
     * - si un ajustement inventaire a fait une sortie, on fait une entrée inverse
     */
    @Transactional
    public void annulerParReference(String reference, String source) {
        List<TransactionStock> transactions =
                transactionStockRepository.findByReferenceDocumentAndSourceDocument(reference, source);

        if (transactions.isEmpty()) {
            return;
        }

        for (TransactionStock tx : transactions) {
            if (tx.getQuantite() == null || tx.getQuantite().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            if (tx.getProduit() == null || tx.getProduit().getId() == null) {
                continue;
            }

            if (tx.getDepot() == null || tx.getDepot().getId() == null) {
                continue;
            }

            BigDecimal quantite = scale3(tx.getQuantite());

            BigDecimal pmp = tx.getPmpApres() != null
                    ? scale6(tx.getPmpApres())
                    : (tx.getPmpAvant() != null ? scale6(tx.getPmpAvant()) : BigDecimal.ZERO);

            Long produitId = tx.getProduit().getId();
            Long depotId = tx.getDepot().getId();

            Long locatorId = null;
            Long stockLotId = null;

            if (isEntree(tx.getTypeMouvement())) {
                sortie(
                        produitId,
                        depotId,
                        locatorId,
                        stockLotId,
                        quantite,
                        pmp,
                        TypeMouvementStock.ANNULATION_RECEPTION_SORTIE,
                        "ANNULATION_INVENTAIRE",
                        reference
                );
            } else {
                entree(
                        produitId,
                        depotId,
                        locatorId,
                        stockLotId,
                        quantite,
                        pmp,
                        TypeMouvementStock.ANNULATION_VENTE_ENTREE,
                        "ANNULATION_INVENTAIRE",
                        reference
                );
            }
        }
    }

    private boolean isEntree(TypeMouvementStock type) {
        return switch (type) {
            case ENTREE_ACHAT,
                 INVENTAIRE_ENTREE,
                 AJUSTEMENT_ENTREE,
                 RETOUR_CLIENT_ENTREE,
                 TRANSFERT_ENTREE,
                 ANNULATION_VENTE_ENTREE,
                 AJUSTEMENT_INVENTAIRE_ENTREE -> true;
            default -> false;
        };
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal scale2(BigDecimal value) {
        return nvl(value).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scale3(BigDecimal value) {
        return nvl(value).setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal scale6(BigDecimal value) {
        return nvl(value).setScale(6, RoundingMode.HALF_UP);
    }
}