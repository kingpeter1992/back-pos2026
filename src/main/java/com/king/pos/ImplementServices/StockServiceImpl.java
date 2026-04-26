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

@Override
@Transactional
public List<StockProduitView> getAllStock() {

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
                        if (r1.getDateCreation() == null) return r2;
                        if (r2.getDateCreation() == null) return r1;
                        return r1.getDateCreation().isAfter(r2.getDateCreation()) ? r1 : r2;
                    }
            ));

    stocks.forEach(stock -> {
        Long produitId = stock.getProduit() != null ? stock.getProduit().getId() : null;
        String produitNom = stock.getProduit() != null ? stock.getProduit().getNom() : null;

        Long categorieId = (stock.getProduit() != null && stock.getProduit().getCategorie() != null)
                ? stock.getProduit().getCategorie().getId()
                : null;

        TarifCategorieProduit regle = categorieId != null ? reglesParCategorie.get(categorieId) : null;

        System.out.println("=================================");
        System.out.println("Stock ID       : " + stock.getId());
        System.out.println("Produit ID     : " + produitId);
        System.out.println("Produit Nom    : " + produitNom);
        System.out.println("Categorie ID   : " + categorieId);
        System.out.println("Quantité Disp  : " + stock.getQuantiteDisponible());
        System.out.println("PMP            : " + stock.getPmp());
        System.out.println("Valeur Stock   : " + stock.getValeurStock());

        if (regle != null) {
            System.out.println("Règle Tarif ID : " + regle.getId());
            System.out.println("Tarif Nom      : " + (regle.getTarifVente() != null ? regle.getTarifVente().getNom() : null));
            System.out.println("Marge %        : " + regle.getTauxMarge());
            System.out.println("Remise Max     : " + regle.getTauxRemiseMax());
            System.out.println("Date Création  : " + regle.getDateCreation());
        } else {
            System.out.println("Règle Tarif    : AUCUNE REGLE TROUVEE");
        }
    });

    return stocks.stream()
            .map(stock -> mapToStockView(stock, reglesParCategorie))
            .toList();
}

   private static final BigDecimal ZERO = BigDecimal.ZERO;

private StockProduitView mapToStockView(
        StockProduit stock,
        Map<Long, TarifCategorieProduit> reglesParCategorie
) {
    Produit produit = stock.getProduit();
    Depot depot = stock.getDepot();

    Long categorieId = (produit != null && produit.getCategorie() != null)
            ? produit.getCategorie().getId()
            : null;

    TarifCategorieProduit regle = categorieId != null
            ? reglesParCategorie.get(categorieId)
            : null;

    BigDecimal quantite = nvl(stock.getQuantiteDisponible()).setScale(3, RoundingMode.HALF_UP);
    BigDecimal pmp = nvl(stock.getPmp()).setScale(2, RoundingMode.HALF_UP);
    
    ProduitLocator produitLocator = produitLocatorRepository
    .findByProduitIdAndDepotId(stock.getProduit().getId(), stock.getDepot().getId())
    .orElse(null);

    String locatorCode = produitLocator != null ? produitLocator.getLocator().getCode() : null;
    Long locatorId = produitLocator != null ? produitLocator.getLocator().getId() : null;

    BigDecimal stockMinimum = (produit != null && produit.getStockMinimum() != null)
            ? produit.getStockMinimum().setScale(2, RoundingMode.HALF_UP)
            : ZERO.setScale(2, RoundingMode.HALF_UP);

    BigDecimal stockMaximum = (produit != null && produit.getStockMaximum() != null)
            ? produit.getStockMaximum().setScale(2, RoundingMode.HALF_UP)
            : ZERO.setScale(2, RoundingMode.HALF_UP);

    BigDecimal tauxMarge = (regle != null && regle.getTauxMarge() != null)
            ? regle.getTauxMarge().setScale(2, RoundingMode.HALF_UP)
            : ZERO.setScale(2, RoundingMode.HALF_UP);

    BigDecimal margeUnitaire = pmp.multiply(tauxMarge)
            .divide(CENT, 2, RoundingMode.HALF_UP);

    BigDecimal prixVenteUnitaire = pmp.add(margeUnitaire)
            .setScale(2, RoundingMode.HALF_UP);

    BigDecimal margeTotaleStock = margeUnitaire.multiply(quantite)
            .setScale(2, RoundingMode.HALF_UP);

    BigDecimal valeurStock = pmp.multiply(quantite)
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
            .locatorId(locatorId)
            .locatorCode(locatorCode)
            .nomDepot(depot != null ? depot.getNom() : null)
            .quantiteDisponible(quantite)
            .pmp(pmp)
            .valeurStock(valeurStock)
            .stockMinimum(stockMinimum)
            .stockMaximum(stockMaximum)
            .statutStock(resolveStatut(quantite, stockMinimum, stockMaximum))
            .tarifVenteId(regle != null && regle.getTarifVente() != null ? regle.getTarifVente().getId() : null)
            .tarifCode(regle != null && regle.getTarifVente() != null ? regle.getTarifVente().getCode() : null)
            .tarifNom(regle != null && regle.getTarifVente() != null ? regle.getTarifVente().getNom() : null)
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

    @Override
    public List<TransactionStockView> getAllMouvements() {
        return transactionStockRepository.findAllByOrderByDateTransactionDesc()
                .stream()
                .map(this::mapTransactionToView)
                .toList();
    }

    private TransactionStockView mapTransactionToView(TransactionStock transaction) {
        return TransactionStockView.builder()
                .id(transaction.getId())
                .dateTransaction(transaction.getDateTransaction())
                .typeTransaction(transaction.getTypeMouvement() != null
                        ? transaction.getTypeMouvement().name()
                        : null)

                .produitId(transaction.getProduit() != null ? transaction.getProduit().getId() : null)
                .produitNom(transaction.getProduit() != null ? transaction.getProduit().getNom() : null)

                .depotId(transaction.getDepot() != null ? transaction.getDepot().getId() : null)
                .depotNom(transaction.getDepot() != null ? transaction.getDepot().getNom() : null)

                .quantite(nvl(transaction.getQuantite()))
                .stockAvant(nvl(transaction.getStockAvant()))
                .stockApres(nvl(transaction.getStockApres()))
                .pmpAvant(nvl(transaction.getPmpAvant()))
                .pmpApres(nvl(transaction.getPmpApres()))
                .prixUnitaire(nvl(transaction.getPrixUnitaire()))
                .fraisUnitaire(nvl(transaction.getFraisUnitaire()))
                .coutUnitaireFinal(nvl(transaction.getCoutUnitaireFinal()))

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

}
