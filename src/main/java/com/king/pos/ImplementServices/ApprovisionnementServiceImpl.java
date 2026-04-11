package com.king.pos.ImplementServices;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.king.pos.Dao.ParametreApprovisionnementRepository;
import com.king.pos.Dao.ProduitRepository;
import com.king.pos.Dao.StockRepository;
import com.king.pos.Dao.VenteLigneRepository;
import com.king.pos.Dto.Response.SuggestionApproResponse;
import com.king.pos.Entitys.ParametreApprovisionnement;
import com.king.pos.Entitys.Produit;
import com.king.pos.Interface.ApprovisionnementService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ApprovisionnementServiceImpl implements ApprovisionnementService {

    private final ProduitRepository produitRepository;
    private final VenteLigneRepository venteLigneRepository;
    private final StockRepository stockProduitRepository;
    private final ParametreApprovisionnementRepository parametreApprovisionnementRepository;

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal THIRTY = BigDecimal.valueOf(30);

    @Override
    public List<SuggestionApproResponse> getSuggestionsReapprovisionnement(Integer joursCouverture) {

        ParametreApprovisionnement config = getConfig();

        int delaiApprovisionnement = safeInt(config.getDelaiApprovisionnementJours(), 5);
        int joursSecurite = safeInt(config.getJoursSecurite(), 3);
        int couvertureCible = (joursCouverture != null && joursCouverture > 0)
                ? joursCouverture
                : safeInt(config.getCouvertureCibleJours(), 15);

        LocalDateTime dateFin = LocalDateTime.now();
        LocalDateTime dateDebut = dateFin.minusDays(30);

        List<Produit> produits = produitRepository.findAll();

        Map<Long, BigDecimal> ventes30JoursMap = loadVentes30Jours(dateDebut, dateFin);
        Map<Long, BigDecimal> stockMap = loadStockActuel();

        List<SuggestionApproResponse> result = new ArrayList<>();

        for (Produit produit : produits) {
            BigDecimal stockActuel = stockMap.getOrDefault(produit.getId(), ZERO);
            BigDecimal quantiteVendue30Jours = ventes30JoursMap.getOrDefault(produit.getId(), ZERO);

            BigDecimal rotationJournaliere = quantiteVendue30Jours
                    .divide(THIRTY, 3, RoundingMode.HALF_UP);

            BigDecimal stockSecurite = rotationJournaliere
                    .multiply(BigDecimal.valueOf(joursSecurite))
                    .setScale(3, RoundingMode.HALF_UP);

            BigDecimal pointCommande = rotationJournaliere
                    .multiply(BigDecimal.valueOf(delaiApprovisionnement))
                    .add(stockSecurite)
                    .setScale(3, RoundingMode.HALF_UP);

            BigDecimal stockCible = rotationJournaliere
                    .multiply(BigDecimal.valueOf(couvertureCible))
                    .setScale(3, RoundingMode.HALF_UP);

            BigDecimal quantiteACommander = stockCible.subtract(stockActuel);
            if (quantiteACommander.compareTo(ZERO) < 0) {
                quantiteACommander = ZERO;
            }
            quantiteACommander = quantiteACommander.setScale(3, RoundingMode.HALF_UP);

            String statutAppro = determinerStatut(stockActuel, pointCommande, stockCible);
            Integer joursCouvertureRestants = calculerJoursCouverture(stockActuel, rotationJournaliere);

            result.add(
                    SuggestionApproResponse.builder()
                            .produitId(produit.getId())
                            .produitNom(produit.getNom())
                            .codeBarres(produit.getCodeBarres())
                            .categorieNom(produit.getCategorie() != null ? produit.getCategorie().getNom() : null)
                            .stockActuel(stockActuel.setScale(3, RoundingMode.HALF_UP))
                            .quantiteVendue30Jours(quantiteVendue30Jours.setScale(3, RoundingMode.HALF_UP))
                            .rotationJournaliere(rotationJournaliere)
                            .delaiApprovisionnementJours(delaiApprovisionnement)
                            .joursSecurite(joursSecurite)
                            .couvertureCibleJours(couvertureCible)
                            .stockSecurite(stockSecurite)
                            .pointCommande(pointCommande)
                            .stockCible(stockCible)
                            .quantiteACommander(quantiteACommander)
                            .statutAppro(statutAppro)
                            .joursCouvertureRestants(joursCouvertureRestants)
                            .build()
            );
        }

        return result.stream()
                .sorted(Comparator
                        .comparing((SuggestionApproResponse s) -> prioriteStatut(s.getStatutAppro()))
                        .thenComparing(SuggestionApproResponse::getQuantiteACommander, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    private ParametreApprovisionnement getConfig() {
        return parametreApprovisionnementRepository.findFirstByActifTrue()
                .orElseGet(() -> ParametreApprovisionnement.builder()
                        .delaiApprovisionnementJours(5)
                        .joursSecurite(3)
                        .couvertureCibleJours(15)
                        .actif(true)
                        .build());
    }

    private Map<Long, BigDecimal> loadVentes30Jours(LocalDateTime dateDebut, LocalDateTime dateFin) {
        List<Object[]> rows = venteLigneRepository.getQuantitesVenduesParProduit(dateDebut, dateFin);
        Map<Long, BigDecimal> map = new HashMap<>();

        for (Object[] row : rows) {
            Long produitId = ((Number) row[0]).longValue();
            BigDecimal quantite = toBigDecimal(row[1]);
            map.put(produitId, quantite);
        }

        return map;
    }

    private Map<Long, BigDecimal> loadStockActuel() {
        List<Object[]> rows = stockProduitRepository.findStockAndPmpByProduit();
        Map<Long, BigDecimal> map = new HashMap<>();

        for (Object[] row : rows) {
            Long produitId = ((Number) row[0]).longValue();
            BigDecimal stock = toBigDecimal(row[1]);
            map.put(produitId, stock);
        }

        return map;
    }

    private String determinerStatut(BigDecimal stockActuel, BigDecimal pointCommande, BigDecimal stockCible) {
        if (stockActuel.compareTo(ZERO) <= 0) {
            return "RUPTURE";
        }

        if (stockActuel.compareTo(pointCommande) <= 0) {
            return "A_COMMANDER";
        }

        if (stockActuel.compareTo(stockCible.multiply(BigDecimal.valueOf(1.5))) > 0) {
            return "SURSTOCK";
        }

        return "NORMAL";
    }

    private Integer calculerJoursCouverture(BigDecimal stockActuel, BigDecimal rotationJournaliere) {
        if (rotationJournaliere.compareTo(ZERO) <= 0) {
            return null;
        }

        return stockActuel
                .divide(rotationJournaliere, 0, RoundingMode.DOWN)
                .intValue();
    }

    private int prioriteStatut(String statut) {
        return switch (statut) {
            case "RUPTURE" -> 1;
            case "A_COMMANDER" -> 2;
            case "NORMAL" -> 3;
            case "SURSTOCK" -> 4;
            default -> 99;
        };
    }

    private int safeInt(Integer value, int defaultValue) {
        return value != null ? value : defaultValue;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return ZERO;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Integer i) return BigDecimal.valueOf(i);
        if (value instanceof Long l) return BigDecimal.valueOf(l);
        if (value instanceof Double d) return BigDecimal.valueOf(d);
        if (value instanceof Float f) return BigDecimal.valueOf(f);
        return new BigDecimal(value.toString());
    }
}