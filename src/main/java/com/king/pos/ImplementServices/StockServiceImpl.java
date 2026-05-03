package com.king.pos.ImplementServices;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.king.pos.Dao.ProduitLocatorRepository;
import com.king.pos.Dao.ProduitRepository;
import com.king.pos.Dao.StockRepository;
import com.king.pos.Dao.TarifCategorieProduitRepository;
import com.king.pos.Dao.TauxChangeRepository;
import com.king.pos.Dao.TransactionStockRepository;
import com.king.pos.Dto.TransactionStockView;
import com.king.pos.Dto.Response.ProduitPosResponse;
import com.king.pos.Dto.Response.StockAlerteView;
import com.king.pos.Dto.Response.StockProduitView;
import com.king.pos.Entitys.Depot;
import com.king.pos.Entitys.ImagePhoto;
import com.king.pos.Entitys.Produit;
import com.king.pos.Entitys.ProduitLocator;
import com.king.pos.Entitys.StockProduit;
import com.king.pos.Entitys.TarifCategorieProduit;
import com.king.pos.Entitys.TauxChange;
import com.king.pos.Entitys.TransactionStock;
import com.king.pos.Interface.StockService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StockServiceImpl implements StockService {

    private static final BigDecimal CENT = new BigDecimal("100");

    private final StockRepository stockRepository;
    private final ProduitRepository produitRepository;
    private final StockRepository stockProduitRepository;
    private final TarifCategorieProduitRepository tarifCategorieProduitRepository;
    private final ProduitLocatorRepository produitLocatorRepository;
    private final TauxChangeRepository tauxChangeRepository;

    @Override
    @Transactional
    public List<StockProduitView> getAllStock() {

        BigDecimal tauxChange = tauxChangeRepository
                .findTopByActifTrueOrderByDateActivationDescDateCreationDesc()
                .map(TauxChange::getTaux)
                .orElse(BigDecimal.ONE);

        List<StockProduit> stocks = stockRepository.findAllAvecProduitCategorieEtDepot();

        Map<Long, TarifCategorieProduit> reglesParCategorie = tarifCategorieProduitRepository
                .findLatestActifByCategorie()
                .stream()
                .filter(regle -> regle != null
                        && Boolean.TRUE.equals(regle.getActif())
                        && regle.getCategorie() != null
                        && regle.getCategorie().getId() != null)
                .collect(Collectors.toMap(
                        regle -> regle.getCategorie().getId(),
                        Function.identity(),
                        (r1, r2) -> {
                            if (r1.getDateCreation() == null)
                                return r2;
                            if (r2.getDateCreation() == null)
                                return r1;
                            return r1.getDateCreation().isAfter(r2.getDateCreation()) ? r1 : r2;
                        }));

          
    return stocks.stream()
            .map(stock -> mapToStockView(stock, reglesParCategorie))
            .toList();
    }

    private static final BigDecimal ZERO = BigDecimal.ZERO;
private StockProduitView mapToStockView(
        StockProduit stock,
        Map<Long, TarifCategorieProduit> reglesParCategorie) {

    Produit produit = stock.getProduit();
    Depot depot = stock.getDepot();

    Long categorieId = produit != null && produit.getCategorie() != null
            ? produit.getCategorie().getId()
            : null;

    TarifCategorieProduit regle = categorieId != null
            ? reglesParCategorie.get(categorieId)
            : null;

    BigDecimal quantite = nvl(stock.getQuantiteDisponible()).setScale(3, RoundingMode.HALF_UP);

    // ===== HISTORIQUE STOCK =====
    BigDecimal tauxChangeUtilise = nvl(stock.getTauxChangeUtilise()).setScale(2, RoundingMode.HALF_UP);

    BigDecimal pmpFc = nvl(stock.getPmpFc()).setScale(2, RoundingMode.HALF_UP);

    BigDecimal pmpUsd = nvl(stock.getPmpUsd()).setScale(4, RoundingMode.HALF_UP);

    BigDecimal valeurStockFc = nvl(stock.getValeurStockFc()).setScale(2, RoundingMode.HALF_UP);

    BigDecimal valeurStockUsd = nvl(stock.getValeurStockUsd()).setScale(2, RoundingMode.HALF_UP);

    // ===== TARIFICATION FC =====
    BigDecimal tauxMarge = regle != null && regle.getTauxMarge() != null
            ? regle.getTauxMarge().setScale(2, RoundingMode.HALF_UP)
            : ZERO.setScale(2, RoundingMode.HALF_UP);

    BigDecimal margeUnitaire = pmpFc.multiply(tauxMarge)
            .divide(CENT, 2, RoundingMode.HALF_UP);

    BigDecimal prixVenteUnitaire = pmpFc.add(margeUnitaire)
            .setScale(2, RoundingMode.HALF_UP);

    BigDecimal margeTotaleStock = margeUnitaire.multiply(quantite)
            .setScale(2, RoundingMode.HALF_UP);

    return StockProduitView.builder()
            .stockId(stock.getId())
            .produitId(produit != null ? produit.getId() : null)
            .nomProduit(produit != null ? produit.getNom() : null)
            .codeBarre(produit != null ? produit.getCodeBarres() : null)

            .categorieId(categorieId)
            .categorie(produit != null && produit.getCategorie() != null
                    ? produit.getCategorie().getNom()
                    : null)

            .depotId(depot != null ? depot.getId() : null)
            .nomDepot(depot != null ? depot.getNom() : null)

            .quantiteDisponible(quantite)

            .pmp(pmpFc)
            .valeurStock(valeurStockFc)

            .tauxChangeUtilise(tauxChangeUtilise)
            .pmpFc(pmpFc)
            .pmpUsd(pmpUsd)
            .valeurStockFc(valeurStockFc)
            .valeurStockUsd(valeurStockUsd)

            .stockMinimum(produit.getStockMinimum())
            .stockMaximum(produit.getStockMaximum())

            .statutStock(resolveStatut(
                    quantite,
                    produit.getStockMinimum(),
                    produit.getStockMaximum()
            ))

            .tauxMarge(tauxMarge)
            .margeUnitaire(margeUnitaire)
            .prixVenteUnitaire(prixVenteUnitaire)
            .margeTotaleStock(margeTotaleStock)

            .build();
}

    private String resolveStatut(BigDecimal quantite, BigDecimal stockMinimum, BigDecimal stockMaximum) {
        if (quantite.compareTo(ZERO) <= 0) {
            return "RUPTURE";
        }

        if (stockMinimum.compareTo(ZERO) > 0 && quantite.compareTo(stockMinimum) < 0) {
            return "ALERTE_RUPTURE";
        }

        if (stockMaximum.compareTo(ZERO) > 0 && quantite.compareTo(stockMaximum) > 0) {
            return "SURPLUS";
        }

        return "NORMAL";
    }

    @Override
    public List<StockAlerteView> getAlertesStock() {
        return stockRepository.findAlertesStock();
    }

    @Override
    public Optional<StockProduit> getStockByProduitId(Long produitId) {
        return stockRepository.findByProduitId(produitId);
    }

    @Override
    public Optional<StockProduit> getStockByProduitIdAndDepotId(Long produitId, Long depotId) {
        return stockRepository.findByProduitIdAndDepotId(produitId, depotId);
    }

    @Override
    public void validateQuantitesProduit(Produit produit) {
        if (produit == null) {
            throw new EntityNotFoundException("Produit introuvable.");
        }

        BigDecimal stockMinimum = produit.getStockMinimum() != null ? produit.getStockMinimum() : BigDecimal.ZERO;
        BigDecimal stockMaximum = produit.getStockMaximum() != null ? produit.getStockMaximum() : BigDecimal.ZERO;

        if (stockMinimum.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Le stock minimum ne peut pas être négatif.");
        }

        if (stockMaximum.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Le stock maximum ne peut pas être négatif.");
        }

        if (stockMaximum.compareTo(BigDecimal.ZERO) > 0 &&
                stockMaximum.compareTo(stockMinimum) < 0) {
            throw new IllegalStateException("Le stock maximum doit être supérieur ou égal au stock minimum.");
        }

        produit.setStockMinimum(stockMinimum);
        produit.setStockMaximum(stockMaximum);
    }

    private String resolveStatut(StockProduit stock) {
        BigDecimal quantite = nvl(stock.getQuantiteDisponible());

        BigDecimal min = stock.getProduit() != null && stock.getProduit().getStockMinimum() != null
                ? stock.getProduit().getStockMinimum()
                : BigDecimal.ZERO;
        BigDecimal max = stock.getProduit() != null && stock.getProduit().getStockMaximum() != null
                ? stock.getProduit().getStockMaximum()
                : BigDecimal.ZERO;

        if (quantite.compareTo(BigDecimal.ZERO) <= 0) {
            return "RUPTURE";
        }
        if (min.compareTo(BigDecimal.ZERO) > 0 && quantite.compareTo(min) < 0) {
            return "ALERTE_RUPTURE";
        }
        if (max.compareTo(BigDecimal.ZERO) > 0 && quantite.compareTo(max) > 0) {
            return "SURPLUS";
        }
        return "NORMAL";
    }

    @Override
    @Transactional
    public void ajouterStock(Long produitId, Integer quantite, String reference, String commentaire) {
        if (produitId == null) {
            throw new IllegalStateException("Le produit est obligatoire.");
        }

        if (quantite == null || quantite <= 0) {
            throw new IllegalStateException("La quantité à ajouter doit être supérieure à zéro.");
        }

        StockProduit stock = stockRepository.findByProduitId(produitId)
                .orElseThrow(() -> new EntityNotFoundException("Stock introuvable pour le produit ID : " + produitId));

        BigDecimal quantiteAjout = BigDecimal.valueOf(quantite);

        stock.setQuantiteDisponible(nvl(stock.getQuantiteDisponible()).add(quantiteAjout));
        stock.setDateDerniereEntree(LocalDateTime.now());

        recalculateValeurStock(stock);

        stockRepository.save(stock);
    }

    @Override
    @Transactional
    public void retirerStock(Long produitId, Integer quantite, String reference, String commentaire) {
        if (produitId == null) {
            throw new IllegalStateException("Le produit est obligatoire.");
        }

        if (quantite == null || quantite <= 0) {
            throw new IllegalStateException("La quantité à retirer doit être supérieure à zéro.");
        }

        StockProduit stock = stockRepository.findByProduitId(produitId)
                .orElseThrow(() -> new EntityNotFoundException("Stock introuvable pour le produit ID : " + produitId));

        BigDecimal quantiteRetrait = BigDecimal.valueOf(quantite);
        BigDecimal quantiteDisponible = nvl(stock.getQuantiteDisponible());

        if (quantiteDisponible.compareTo(quantiteRetrait) < 0) {
            throw new IllegalStateException("Stock insuffisant pour effectuer la sortie.");
        }

        BigDecimal nouveauStock = quantiteDisponible.subtract(quantiteRetrait);

        stock.setQuantiteDisponible(nouveauStock);
        stock.setDateDerniereSortie(LocalDateTime.now());

        recalculateValeurStock(stock);

        stockRepository.save(stock);

        // 🔥 Alerte stock sécurité
        Produit produit = stock.getProduit();

        if (produit != null && produit.getStockMinimum() != null) {

            BigDecimal stockMinimum = produit.getStockMinimum();

            if (nouveauStock.compareTo(stockMinimum) <= 0) {
                System.out.println("ALERTE STOCK: seuil de sécurité atteint pour le produit "
                        + produit.getNom());
            }
        }
    }

    private void recalculateValeurStock(StockProduit stock) {
        BigDecimal quantite = nvl(stock.getQuantiteDisponible());
        BigDecimal pmp = nvl(stock.getPmp());

        stock.setValeurStock(quantite.multiply(pmp));
    }

    @Override
    @Transactional
    public List<ProduitPosResponse> getProduitsPos() {

        List<Produit> produits = produitRepository.findAll();

        Map<Long, BigDecimal> stockMap = new HashMap<>();
        Map<Long, BigDecimal> pmpMap = new HashMap<>();

        List<Object[]> stockRows = stockProduitRepository.findStockAndPmpByProduit();

        for (Object[] row : stockRows) {
            Long produitId = ((Number) row[0]).longValue();
            BigDecimal stock = toBigDecimal(row[1]);
            BigDecimal pmp = toBigDecimal(row[2]);

            stockMap.put(produitId, stock);
            pmpMap.put(produitId, pmp);

            // System.err.println("Produit ID: " + produitId + ", Stock: " + stock + ", PMP:
            // " + pmp);
        }

        return produits.stream()
                .map(produit -> mapProduitToPosResponse(produit, stockMap, pmpMap))
                .toList();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal bd) {
            return bd;
        }

        if (value instanceof Integer i) {
            return BigDecimal.valueOf(i);
        }

        if (value instanceof Long l) {
            return BigDecimal.valueOf(l);
        }

        if (value instanceof Double d) {
            return BigDecimal.valueOf(d);
        }

        if (value instanceof Float f) {
            return BigDecimal.valueOf(f.doubleValue());
        }

        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }

        return new BigDecimal(value.toString());
    }

    private ProduitPosResponse mapProduitToPosResponse(
            Produit produit,
            Map<Long, BigDecimal> stockMap,
            Map<Long, BigDecimal> pmpMap) {
        String imageUrl = null;

        if (produit.getImages() != null && !produit.getImages().isEmpty()) {
            ImagePhoto principale = produit.getImages().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getPrincipale()))
                    .findFirst()
                    .orElse(produit.getImages().get(0));

            imageUrl = principale.getUrl();
        }

        return ProduitPosResponse.builder()
                .id(produit.getId())
                .nom(produit.getNom())
                .codeBarres(produit.getCodeBarres())
                .description(produit.getDescription())
                .prixVente(produit.getPrixVente() != null ? produit.getPrixVente() : BigDecimal.ZERO)
                .prixAchat(produit.getPrixAchat() != null ? produit.getPrixAchat() : BigDecimal.ZERO)

                .stock(stockMap.getOrDefault(produit.getId(), BigDecimal.ZERO))
                .pmp(pmpMap.getOrDefault(produit.getId(), BigDecimal.ZERO))
                .stockSecurite(
                        produit.getStockMinimum() != null
                                ? produit.getStockMinimum()
                                : BigDecimal.ZERO)

                .stockMinimum(produit.getStockMinimum())
                .stockMaximum(produit.getStockMaximum())

                .imageUrl(imageUrl)
                .actif(produit.getActif() != null ? produit.getActif() : true)

                .categorie(
                        produit.getCategorie() != null
                                ? produit.getCategorie().getNom()
                                : null)

                // .fournisseurNom(
                // produit.getFournisseurPrincipal() != null
                // ? produit.getFournisseurPrincipal().getNom()
                // : null
                // )

                .dateCreation(
                        produit.getDateCreation() != null
                                ? produit.getDateCreation().toLocalDate()
                                : null)
                .build();
    }

    final TransactionStockRepository transactionStockRepository;

    private TransactionStockView mapTransactionToView(TransactionStock transaction) {

    BigDecimal quantite = nvl(transaction.getQuantite())
            .setScale(3, RoundingMode.HALF_UP);

    BigDecimal stockAvant = nvl(transaction.getStockAvant())
            .setScale(3, RoundingMode.HALF_UP);

    BigDecimal stockApres = nvl(transaction.getStockApres())
            .setScale(3, RoundingMode.HALF_UP);

    BigDecimal pmpAvantFc = nvl(transaction.getPmpAvant())
            .setScale(6, RoundingMode.HALF_UP);

    BigDecimal pmpApresFc = nvl(transaction.getPmpApres())
            .setScale(6, RoundingMode.HALF_UP);

    BigDecimal prixUnitaireFc = nvl(transaction.getPrixUnitaire())
            .setScale(6, RoundingMode.HALF_UP);

    BigDecimal fraisUnitaireFc = nvl(transaction.getFraisUnitaire())
            .setScale(6, RoundingMode.HALF_UP);

    BigDecimal coutUnitaireFinalFc = nvl(transaction.getCoutUnitaireFinal())
            .setScale(6, RoundingMode.HALF_UP);

    BigDecimal tauxChangeUtilise = nvl(transaction.getTauxChangeUtilise());

    if (tauxChangeUtilise.compareTo(BigDecimal.ZERO) <= 0) {
        tauxChangeUtilise = BigDecimal.ONE;
    }

    tauxChangeUtilise = tauxChangeUtilise.setScale(6, RoundingMode.HALF_UP);

    BigDecimal pmpAvantUsd = pmpAvantFc.divide(tauxChangeUtilise, 6, RoundingMode.HALF_UP);
    BigDecimal pmpApresUsd = pmpApresFc.divide(tauxChangeUtilise, 6, RoundingMode.HALF_UP);

    BigDecimal coutUnitaireFinalUsd = coutUnitaireFinalFc.divide(tauxChangeUtilise, 6, RoundingMode.HALF_UP);

    BigDecimal valeurMouvementFc = quantite.abs()
            .multiply(coutUnitaireFinalFc)
            .setScale(2, RoundingMode.HALF_UP);

    BigDecimal valeurMouvementUsd = valeurMouvementFc
            .divide(tauxChangeUtilise, 2, RoundingMode.HALF_UP);

    BigDecimal valeurStockFc = stockApres
            .multiply(pmpApresFc)
            .setScale(2, RoundingMode.HALF_UP);

    BigDecimal valeurStockUsd = valeurStockFc
            .divide(tauxChangeUtilise, 2, RoundingMode.HALF_UP);

    return TransactionStockView.builder()
            .id(transaction.getId())
            .dateTransaction(transaction.getDateTransaction())

            .produitId(transaction.getProduit() != null ? transaction.getProduit().getId() : null)
            .produitNom(transaction.getProduit() != null ? transaction.getProduit().getNom() : null)

            .depotId(transaction.getDepot() != null ? transaction.getDepot().getId() : null)
            .depotNom(transaction.getDepot() != null ? transaction.getDepot().getNom() : null)

            .typeTransaction(transaction.getTypeMouvement() != null
                    ? transaction.getTypeMouvement().name()
                    : null)

            .quantite(quantite)
            .stockAvant(stockAvant)
            .stockApres(stockApres)

            // Valeurs historiques principales en FC
            .pmpAvant(pmpAvantFc)
            .pmpApres(pmpApresFc)

            .prixUnitaire(prixUnitaireFc)
            .fraisUnitaire(fraisUnitaireFc)
            .coutUnitaireFinal(coutUnitaireFinalFc)

            .tauxChangeUtilise(tauxChangeUtilise)

            // PMP après mouvement
            .pmpFc(pmpApresFc)
            .pmpUsd(pmpApresUsd)

            .valeurStockFc(valeurStockFc)
            .valeurStockUsd(valeurStockUsd)

            .coutUnitaireFinalFc(coutUnitaireFinalFc)
            .coutUnitaireFinalUsd(coutUnitaireFinalUsd)

            .valeurMouvementFc(valeurMouvementFc)
            .valeurMouvementUsd(valeurMouvementUsd)

            .referenceDocument(transaction.getReferenceDocument())
            .sourceDocument(transaction.getSourceDocument())
            .sourceDocumentId(transaction.getSourceDocumentId())
            .libelle(transaction.getLibelle())
            .utilisateur(transaction.getUtilisateur())

            .build();
}
    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }


@Override
public List<TransactionStockView> getAllMouvements() {
    return transactionStockRepository.findAllByOrderByDateTransactionDesc()
            .stream()
            .map(this::mapTransactionToView)
            .toList();
}
}
