package com.king.pos.ImplementServices;


import com.king.pos.Dao.InventaireBordereauLigneRepository;
import com.king.pos.Dao.InventaireBordereauRepository;
import com.king.pos.Dao.InventaireLigneRepository;
import com.king.pos.Dao.InventaireRepository;
import com.king.pos.Dao.InventaireVarianceRepository;
import com.king.pos.Dto.Response.InventaireVarianceLigneResponse;
import com.king.pos.Dto.Response.InventaireVarianceResponse;
import com.king.pos.Dto.Response.InventaireVarianceResumeResponse;
import com.king.pos.Entitys.Inventaire;
import com.king.pos.Entitys.InventaireBordereau;
import com.king.pos.Entitys.InventaireBordereauLigne;
import com.king.pos.Entitys.InventaireLigne;
import com.king.pos.Entitys.InventaireVariance;
import com.king.pos.Handllers.BusinessException;
import com.king.pos.enums.StatutBordereauInventaire;
import com.king.pos.enums.StatutInventaire;
import com.king.pos.enums.TypeVariance;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventaireVarianceService {

    private final InventaireRepository inventaireRepository;
    private final InventaireLigneRepository inventaireLigneRepository;
    private final InventaireVarianceRepository varianceRepository;
    private final InventaireBordereauRepository bordereauRepository;
    private final InventaireBordereauLigneRepository bordereauLigneRepository;


    @Transactional
    public void lancerVariances(Long inventaireId) {
        Inventaire inventaire = inventaireRepository.findById(inventaireId)
                .orElseThrow(() -> new BusinessException("Inventaire introuvable"));

        if (inventaire.getStatut() == StatutInventaire.VALIDE ||
                inventaire.getStatut() == StatutInventaire.CLOTURE) {
            throw new BusinessException("Impossible de lancer les variances sur un inventaire validé ou clôturé.");
        }

        List<InventaireLigne> articles = inventaireLigneRepository.findByInventaireId(inventaireId);

        boolean auMoinsUnCompte = articles.stream().anyMatch(a -> Boolean.TRUE.equals(a.getCompte()));
        if (!auMoinsUnCompte) {
            throw new BusinessException("Aucun article compté. Impossible de lancer les variances.");
        }

//varianceRepository.deleteByBordereauId(1);

        for (InventaireLigne a : articles) {
            if (!Boolean.TRUE.equals(a.getCompte())) {
                continue;
            }

            BigDecimal theorique = nvl(a.getStockTheorique());
            BigDecimal physique = nvl(a.getStockPhysiqueRetenu());
            BigDecimal ecart = physique.subtract(theorique);
            BigDecimal pmp = nvl(a.getPmp());
            BigDecimal valeur = ecart.multiply(pmp);

            TypeVariance type = TypeVariance.NEANT;
            if (ecart.compareTo(BigDecimal.ZERO) > 0) {
                type = TypeVariance.ENTREE;
            } else if (ecart.compareTo(BigDecimal.ZERO) < 0) {
                type = TypeVariance.SORTIE;
            }

            varianceRepository.save(
                    InventaireVariance.builder()
                            .inventaire(inventaire)
                            .inventaireArticle(a)
                            .stockTheorique(theorique)
                            .stockPhysiqueRetenu(physique)
                            .ecart(ecart)
                            .pmp(pmp)
                            .valeurEcart(valeur)
                            .type(type)
                            .appliquee(false)
                            .build()
            );

            a.setVarianceGeneree(true);
        }

        inventaire.setVarianceLancee(true);
        inventaire.setStatut(StatutInventaire.VARIANCE_LANCEE);
    }

    @Transactional
    public List<InventaireVarianceResponse> getVariances(Long inventaireId) {
        return varianceRepository.findByInventaireId(inventaireId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private InventaireVarianceResponse mapToResponse(InventaireVariance v) {
        return InventaireVarianceResponse.builder()
                .id(v.getId())
                .inventaireId(v.getInventaire() != null ? v.getInventaire().getId() : null)
                .inventaireArticleId(v.getInventaireArticle() != null ? v.getInventaireArticle().getId() : null)

                .produitId(
                        v.getInventaireArticle() != null && v.getInventaireArticle().getProduit() != null
                                ? v.getInventaireArticle().getProduit().getId()
                                : null
                )
                .produitNom(
                        v.getInventaireArticle() != null && v.getInventaireArticle().getProduit() != null
                                ? v.getInventaireArticle().getProduit().getNom()
                                : null
                )
                .codeBarres(
                        v.getInventaireArticle() != null && v.getInventaireArticle().getProduit() != null
                                ? v.getInventaireArticle().getProduit().getCodeBarres()
                                : null
                )

                .categorieId(
                        v.getInventaireArticle() != null &&
                        v.getInventaireArticle().getProduit() != null &&
                        v.getInventaireArticle().getProduit().getCategorie() != null
                                ? v.getInventaireArticle().getProduit().getCategorie().getId()
                                : null
                )
                .categorieNom(
                        v.getInventaireArticle() != null &&
                        v.getInventaireArticle().getProduit() != null &&
                        v.getInventaireArticle().getProduit().getCategorie() != null
                                ? v.getInventaireArticle().getProduit().getCategorie().getNom()
                                : null
                )

                .depotId(
                        v.getInventaireArticle() != null && v.getInventaireArticle().getDepot() != null
                                ? v.getInventaireArticle().getDepot().getId()
                                : null
                )
                .depotNom(
                        v.getInventaireArticle() != null && v.getInventaireArticle().getDepot() != null
                                ? v.getInventaireArticle().getDepot().getNom()
                                : null
                )

                .locatorId(
                        v.getInventaireArticle() != null && v.getInventaireArticle().getLocator() != null
                                ? v.getInventaireArticle().getLocator().getId()
                                : null
                )
                .locatorCode(
                        v.getInventaireArticle() != null && v.getInventaireArticle().getLocator() != null
                                ? v.getInventaireArticle().getLocator().getCode()
                                : null
                )

                .stockTheorique(v.getStockTheorique())
                .stockPhysiqueRetenu(v.getStockPhysiqueRetenu())
                .ecart(v.getEcart())
                .pmp(v.getPmp())
                .valeurEcart(v.getValeurEcart())
                .type(v.getType())
                .appliquee(v.getAppliquee())
                .dateApplication(v.getDateApplication())
                .build();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

@Transactional
public InventaireVarianceResumeResponse getResumeVariances(Long inventaireId) {
    Inventaire inventaire = inventaireRepository.findById(inventaireId)
            .orElseThrow(() -> new BusinessException("Inventaire introuvable"));

    List<InventaireVariance> variances = varianceRepository.findByInventaireId(inventaireId);

    List<InventaireVarianceLigneResponse> lignes = variances.stream()
            .map(this::mapToLigneResponse)
            .toList();

    int totalEntrees = (int) variances.stream()
            .filter(v -> v.getType() == TypeVariance.ENTREE)
            .count();

    int totalSorties = (int) variances.stream()
            .filter(v -> v.getType() == TypeVariance.SORTIE)
            .count();

    int totalNeant = (int) variances.stream()
            .filter(v -> v.getType() == TypeVariance.NEANT)
            .count();

    BigDecimal totalEcartPositif = variances.stream()
            .filter(v -> v.getEcart() != null && v.getEcart().compareTo(BigDecimal.ZERO) > 0)
            .map(InventaireVariance::getEcart)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalEcartNegatif = variances.stream()
            .filter(v -> v.getEcart() != null && v.getEcart().compareTo(BigDecimal.ZERO) < 0)
            .map(InventaireVariance::getEcart)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalValeurPositive = variances.stream()
            .filter(v -> v.getValeurEcart() != null && v.getValeurEcart().compareTo(BigDecimal.ZERO) > 0)
            .map(InventaireVariance::getValeurEcart)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalValeurNegative = variances.stream()
            .filter(v -> v.getValeurEcart() != null && v.getValeurEcart().compareTo(BigDecimal.ZERO) < 0)
            .map(InventaireVariance::getValeurEcart)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalValeurNette = totalValeurPositive.add(totalValeurNegative);

    return InventaireVarianceResumeResponse.builder()
            .inventaireId(inventaire.getId())
            .referenceInventaire(inventaire.getReference())
            .depotNom(inventaire.getDepot() != null ? inventaire.getDepot().getNom() : null)
            .locatorCode(inventaire.getLocator() != null ? inventaire.getLocator().getCode() : null)
            .statut(inventaire.getStatut() != null ? inventaire.getStatut().name() : null)
            .varianceLancee(inventaire.getVarianceLancee())
            .totalLignes(variances.size())
            .totalEntrees(totalEntrees)
            .totalSorties(totalSorties)
            .totalNeant(totalNeant)
            .totalEcartPositif(totalEcartPositif)
            .totalEcartNegatif(totalEcartNegatif)
            .totalValeurPositive(totalValeurPositive)
            .totalValeurNegative(totalValeurNegative)
            .totalValeurNette(totalValeurNette)
            .lignes(lignes)
            .build();
}

private InventaireVarianceLigneResponse mapToLigneResponse(InventaireVariance v) {
    return InventaireVarianceLigneResponse.builder()
            .id(v.getId())
            .produitId(
                    v.getInventaireArticle() != null && v.getInventaireArticle().getProduit() != null
                            ? v.getInventaireArticle().getProduit().getId()
                            : null
            )
            .produitNom(
                    v.getInventaireArticle() != null && v.getInventaireArticle().getProduit() != null
                            ? v.getInventaireArticle().getProduit().getNom()
                            : null
            )
            .codeBarres(
                    v.getInventaireArticle() != null && v.getInventaireArticle().getProduit() != null
                            ? v.getInventaireArticle().getProduit().getCodeBarres()
                            : null
            )
            .categorieNom(
                    v.getInventaireArticle() != null &&
                    v.getInventaireArticle().getProduit() != null &&
                    v.getInventaireArticle().getProduit().getCategorie() != null
                            ? v.getInventaireArticle().getProduit().getCategorie().getNom()
                            : null
            )
            .depotNom(
                    v.getInventaireArticle() != null && v.getInventaireArticle().getDepot() != null
                            ? v.getInventaireArticle().getDepot().getNom()
                            : null
            )
            .locatorCode(
                    v.getInventaireArticle() != null && v.getInventaireArticle().getLocator() != null
                            ? v.getInventaireArticle().getLocator().getCode()
                            : null
            )
            .stockTheorique(v.getStockTheorique())
            .stockPhysiqueRetenu(v.getStockPhysiqueRetenu())
            .ecart(v.getEcart())
            .pmp(v.getPmp())
            .valeurEcart(v.getValeurEcart())
            .type(v.getType())
            .build();
}

 public List<InventaireVarianceResponse> getAllVariances() {
        return varianceRepository.findAllByOrderByIdDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


@Transactional
public void lancerVariancesParBordereau(Long bordereauId) {

    InventaireBordereau bordereau = bordereauRepository.findById(bordereauId)
            .orElseThrow(() -> new BusinessException("Bordereau introuvable"));

    Inventaire inventaire = bordereau.getInventaire();

    if (inventaire == null) {
        throw new BusinessException("Inventaire introuvable pour ce bordereau.");
    }

    if (inventaire.getStatut() == StatutInventaire.CLOTURE) {
        throw new BusinessException("Impossible de lancer les variances sur un inventaire clôturé.");
    }

    if (Boolean.TRUE.equals(bordereau.getStockMisAJour())) {
        throw new BusinessException("Impossible de relancer les variances : le stock de ce bordereau est déjà mis à jour.");
    }

    List<InventaireBordereauLigne> lignes =
            bordereauLigneRepository.findByBordereauId(bordereauId);

    boolean auMoinsUneLigneComptee = lignes.stream()
            .anyMatch(l -> l.getQuantiteComptee() != null);

    if (!auMoinsUneLigneComptee) {
        throw new BusinessException("Aucune ligne comptée. Impossible de lancer les variances.");
    }

varianceRepository.deleteByBordereauId(bordereauId);

    for (InventaireBordereauLigne ligne : lignes) {

        if (ligne.getQuantiteComptee() == null) {
            continue;
        }

        BigDecimal theorique = nvl(ligne.getInventaireArticle().getStockTheorique());
        BigDecimal physique = nvl(ligne.getQuantiteComptee());
        BigDecimal ecart = physique.subtract(theorique);
        BigDecimal pmp = nvl(ligne.getInventaireArticle().getPmp());
        BigDecimal valeur = ecart.multiply(pmp);

        TypeVariance type = TypeVariance.NEANT;

        if (ecart.compareTo(BigDecimal.ZERO) > 0) {
            type = TypeVariance.ENTREE;
        } else if (ecart.compareTo(BigDecimal.ZERO) < 0) {
            type = TypeVariance.SORTIE;
        }
varianceRepository.save(
    InventaireVariance.builder()
        .inventaire(inventaire)
        .inventaireArticle(ligne.getInventaireArticle())
        .bordereau(bordereau)
        .ligneBordereau(ligne)
        .stockTheorique(theorique)
        .stockPhysiqueRetenu(physique)
        .ecart(ecart)
        .pmp(pmp)
        .valeurEcart(valeur)
        .type(type)
        .appliquee(false)
        .build()
);

        ligne.setVarianceGeneree(true);
    }

    bordereau.setVarianceLancee(true);
    
    bordereau.setStatut(StatutBordereauInventaire.VARIANCE_LANCEE);

    inventaire.setVarianceLancee(true);

    if (inventaire.getStatut() != StatutInventaire.CLOTURE) {
        inventaire.setStatut(StatutInventaire.VARIANCE_LANCEE);
    }
}
}