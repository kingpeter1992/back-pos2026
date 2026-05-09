package com.king.pos.ImplementServices;

import com.king.pos.Dao.*;
import com.king.pos.Dto.InventaireResultatKpiResponse;
import com.king.pos.Dto.InventaireResultatLigneResponse;
import com.king.pos.Dto.InventaireResultatResponse;
import com.king.pos.Entitys.*;
import com.king.pos.Handllers.BusinessException;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventaireResultatService {

    private final InventaireRepository inventaireRepository;
    private final InventaireVarianceRepository varianceRepository;
    private final InventaireBordereauRepository bordereauRepository;
    private final StockRepository stockRepository;
    private final InventaireService inventaireService;

@Transactional
public InventaireResultatResponse getResultat(Long inventaireId) {

        Inventaire inventaire = inventaireRepository.findById(inventaireId)
                .orElseThrow(() -> new BusinessException("Inventaire introuvable"));

        List<InventaireVariance> variances =
                varianceRepository.findByInventaireId(inventaireId);

        BigDecimal taux = inventaire.getTauxChangeSnapshot() != null
                ? inventaire.getTauxChangeSnapshot()
                : BigDecimal.ONE;

        List<InventaireResultatLigneResponse> lignes = variances.stream()
                .map(v -> mapLigne(v))
                .toList();

        int totalArticles = variances.size();

        int articlesAvecEcart = (int) variances.stream()
                .filter(v -> nvl(v.getEcart()).compareTo(BigDecimal.ZERO) != 0)
                .count();

        int articlesComptes = totalArticles;

        int articlesSansEcart = totalArticles - articlesAvecEcart;

        BigDecimal totalStockTheo = variances.stream()
                .map(v -> nvl(v.getStockTheorique()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalStockComptee = variances.stream()
                .map(v -> nvl(v.getStockPhysiqueRetenu()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEcartQte = variances.stream()
                .map(v -> nvl(v.getEcart()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valeurTheoCDF = variances.stream()
                .map(v -> nvl(v.getStockTheorique()).multiply(nvl(v.getPmp())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valeurCompteeCDF = variances.stream()
                .map(v -> nvl(v.getStockPhysiqueRetenu()).multiply(nvl(v.getPmp())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valeurEcartCDF = variances.stream()
                .map(v -> nvl(v.getValeurEcart()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valeurPositif = variances.stream()
                .filter(v -> nvl(v.getValeurEcart()).compareTo(BigDecimal.ZERO) > 0)
                .map(InventaireVariance::getValeurEcart)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valeurNegatif = variances.stream()
                .filter(v -> nvl(v.getValeurEcart()).compareTo(BigDecimal.ZERO) < 0)
                .map(InventaireVariance::getValeurEcart)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalBordereaux = bordereauRepository.findByInventaireId(inventaireId).size();

        int bordereauxMajStock = (int) bordereauRepository.findByInventaireId(inventaireId)
                .stream()
                .filter(b -> Boolean.TRUE.equals(b.getStockMisAJour()))
                .count();

        InventaireResultatKpiResponse kpi = InventaireResultatKpiResponse.builder()
                .totalArticles(totalArticles)
                .articlesComptes(articlesComptes)
                .articlesAvecEcart(articlesAvecEcart)
                .articlesSansEcart(articlesSansEcart)

                .pourcentageArticlesAvecEcart(
                        percent(articlesAvecEcart, totalArticles)
                )

                .totalStockTheorique(totalStockTheo)
                .totalStockComptee(totalStockComptee)
                .totalEcartQuantite(totalEcartQte)

                .valeurTheoriqueCDF(valeurTheoCDF)
                .valeurCompteeCDF(valeurCompteeCDF)
                .valeurEcartCDF(valeurEcartCDF)

                .valeurEcartPositifCDF(valeurPositif)
                .valeurEcartNegatifCDF(valeurNegatif)

                .valeurTheoriqueUSD(div(valeurTheoCDF, taux))
                .valeurCompteeUSD(div(valeurCompteeCDF, taux))
                .valeurEcartUSD(div(valeurEcartCDF, taux))

                .pourcentageEcartValeur(percent(valeurEcartCDF, valeurTheoCDF))

                .bordereauxTotal(totalBordereaux)
                .bordereauxMisAJourStock(bordereauxMajStock)

                .stockTotalementMisAJour(
                        totalBordereaux > 0 && totalBordereaux == bordereauxMajStock
                )
                .build();

        return InventaireResultatResponse.builder()
                .inventaire(inventaireService.getById(inventaireId))
                .kpi(kpi)
                .lignes(lignes)
                .build();
    }

    private InventaireResultatLigneResponse mapLigne(InventaireVariance v) {

        StockProduit stockActuel = stockRepository
                .findByProduitIdAndDepotId(
                        v.getInventaireArticle().getProduit().getId(),
                        v.getInventaireArticle().getDepot().getId()
                )
                .orElse(null);

        return InventaireResultatLigneResponse.builder()
                .depot(v.getInventaireArticle().getDepot().getNom())
                .numeroInventaire(v.getInventaire().getReference())
                .numeroBordereau(v.getBordereau() != null ? v.getBordereau().getReference() : null)

                .locator(v.getInventaireArticle().getLocator() != null
                        ? v.getInventaireArticle().getLocator().getCode()
                        : null)

                .statutBordereau(v.getBordereau() != null
                        ? v.getBordereau().getStatut().name()
                        : null)

                .dateInventaire(v.getInventaire().getDateOuverture())
                .dateMiseAJourStock(v.getBordereau() != null
                        ? v.getBordereau().getDateMiseAJourStock()
                        : null)

                .numeroLigne(v.getLigneBordereau() != null
                        ? v.getLigneBordereau().getNumeroLigne()
                        : null)

                .codeArticle(v.getInventaireArticle().getProduit().getCodeBarres())
                .designation(v.getInventaireArticle().getProduit().getNom())

                .quantiteStockTheorique(v.getStockTheorique())
                .quantiteComptee(v.getStockPhysiqueRetenu())
                .quantiteEcart(v.getEcart())

                .ecart(nvl(v.getEcart()).compareTo(BigDecimal.ZERO) != 0)

                .pmpInventaire(v.getPmp())

                .valeurStockTheorique(
                        nvl(v.getStockTheorique()).multiply(nvl(v.getPmp()))
                )

                .valeurStockComptee(
                        nvl(v.getStockPhysiqueRetenu()).multiply(nvl(v.getPmp()))
                )

                .valeurEcart(v.getValeurEcart())

                .typeVariance(v.getType().name())

                .stockMisAJour(v.getBordereau() != null
                        ? v.getBordereau().getStockMisAJour()
                        : false)

                .stockActuel(stockActuel != null ? stockActuel.getQuantiteDisponible() : BigDecimal.ZERO)
                .pmpActuel(stockActuel != null ? stockActuel.getPmp() : BigDecimal.ZERO)
                .valeurStockActuel(stockActuel != null ? stockActuel.getValeurStock() : BigDecimal.ZERO)

                .commentaireComptage(
                        v.getLigneBordereau() != null
                                ? v.getLigneBordereau().getCommentaire()
                                : null
                )
                .build();
    }

    private BigDecimal percent(Number part, Number total) {
        if (total == null || total.doubleValue() == 0) return BigDecimal.ZERO;

        return BigDecimal.valueOf(part.doubleValue())
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total.doubleValue()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal percent(BigDecimal part, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        return part.multiply(BigDecimal.valueOf(100))
                .divide(total, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal div(BigDecimal value, BigDecimal taux) {
        if (taux == null || taux.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        return value.divide(taux, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    @Transactional
  public List<InventaireResultatResponse> getAllResultats() {
    return inventaireRepository.findAll()
            .stream()
            .map(i -> getResultat(i.getId()))
            .toList();
}

public List<InventaireResultatResponse> getResultats(String from, String to) {

    List<Inventaire> inventaires = inventaireRepository.findAll();

    return inventaires.stream()
        .filter(i -> {
            if (i.getDateInventaire() == null) return true;

            if (from != null && i.getDateInventaire().isBefore(LocalDate.parse(from)))
                return false;

            if (to != null && i.getDateInventaire().isAfter(LocalDate.parse(to)))
                return false;

            return true;
        })
        .map(i -> getResultat(i.getId()))
        .toList();
}
}