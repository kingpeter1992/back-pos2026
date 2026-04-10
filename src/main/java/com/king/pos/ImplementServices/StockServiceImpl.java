package com.king.pos.ImplementServices;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.king.pos.Dao.MouvementStockRepository;
import com.king.pos.Dao.ProduitRepository;
import com.king.pos.Dao.StockRepository;
import com.king.pos.Dto.Response.MouvementStockView;
import com.king.pos.Dto.Response.ProduitPosResponse;
import com.king.pos.Dto.Response.StockAlerteView;
import com.king.pos.Dto.Response.StockProduitView;
import com.king.pos.Entitys.ImagePhoto;
import com.king.pos.Entitys.Produit;
import com.king.pos.Entitys.StockProduit;
import com.king.pos.Interface.StockService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StockServiceImpl implements StockService {

private static final String STATUT_RUPTURE = "RUPTURE";
    private static final String STATUT_ALERTE_RUPTURE = "ALERTE_RUPTURE";
    private static final String STATUT_SURPLUS = "SURPLUS";
    private static final String STATUT_NORMAL = "NORMAL";

    private final StockRepository stockRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final ProduitRepository produitRepository;
    private final StockRepository stockProduitRepository;


    @Override
    public List<StockProduitView> getAllStock() {
        return stockRepository.findAllStockView();
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

        int stockMinimum = produit.getStockMinimum() != null ? produit.getStockMinimum() : 0;
        int stockMaximum = produit.getStockMaximum() != null ? produit.getStockMaximum() : 0;

        if (stockMinimum < 0) {
            throw new IllegalStateException("Le stock minimum ne peut pas être négatif.");
        }

        if (stockMaximum < 0) {
            throw new IllegalStateException("Le stock maximum ne peut pas être négatif.");
        }

        if (stockMaximum > 0 && stockMaximum < stockMinimum) {
            throw new IllegalStateException("Le stock maximum doit être supérieur ou égal au stock minimum.");
        }

        produit.setStockMinimum(stockMinimum);
        produit.setStockMaximum(stockMaximum);
    }

    @Override
    public String resolveStatutStock(Produit produit, StockProduit stock) {
        validateQuantitesProduit(produit);

        BigDecimal quantiteDisponible = stock != null
                ? nvl(stock.getQuantiteDisponible())
                : BigDecimal.ZERO;

        BigDecimal stockMinimum = BigDecimal.valueOf(
                produit.getStockMinimum() != null ? produit.getStockMinimum() : 0
        );

        BigDecimal stockMaximum = BigDecimal.valueOf(
                produit.getStockMaximum() != null ? produit.getStockMaximum() : 0
        );

        if (quantiteDisponible.compareTo(BigDecimal.ZERO) <= 0) {
            return STATUT_RUPTURE;
        }

        if (stockMinimum.compareTo(BigDecimal.ZERO) > 0
                && quantiteDisponible.compareTo(stockMinimum) < 0) {
            return STATUT_ALERTE_RUPTURE;
        }

        if (stockMaximum.compareTo(BigDecimal.ZERO) > 0
                && quantiteDisponible.compareTo(stockMaximum) > 0) {
            return STATUT_SURPLUS;
        }

        return STATUT_NORMAL;
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
                .orElseThrow(() ->
                        new EntityNotFoundException("Stock introuvable pour le produit ID : " + produitId)
                );

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
            .orElseThrow(() ->
                    new EntityNotFoundException("Stock introuvable pour le produit ID : " + produitId)
            );

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

        BigDecimal stockMinimum = BigDecimal.valueOf(produit.getStockMinimum());

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

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    @Override
    public List<MouvementStockView> getAllMouvements() {
        return mouvementStockRepository.findAllView();
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

    // System.err.println("Produit ID: " + produitId + ", Stock: " + stock + ", PMP: " + pmp);
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
        Map<Long, BigDecimal> pmpMap
) {
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
                            ? BigDecimal.valueOf(produit.getStockMinimum())
                            : BigDecimal.ZERO
            )

            .stockMinimum(produit.getStockMinimum())
            .stockMaximum(produit.getStockMaximum())

            .imageUrl(imageUrl)
            .actif(produit.getActif() != null ? produit.getActif() : true)

            .categorie(
                    produit.getCategorie() != null
                            ? produit.getCategorie().getNom()
                            : null
            )

            // .fournisseurNom(
            //         produit.getFournisseurPrincipal() != null
            //                 ? produit.getFournisseurPrincipal().getNom()
            //                 : null
            // )

            .dateCreation(
                    produit.getDateCreation() != null
                            ? produit.getDateCreation().toLocalDate()
                            : null
            )
            .build();
}
}

