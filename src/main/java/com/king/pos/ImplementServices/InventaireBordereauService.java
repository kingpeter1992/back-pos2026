package com.king.pos.ImplementServices;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.king.pos.Dao.InventaireBordereauLigneRepository;
import com.king.pos.Dao.InventaireBordereauRepository;
import com.king.pos.Dao.InventaireLigneRepository;
import com.king.pos.Dao.InventaireRepository;
import com.king.pos.Dao.LocatorRepository;
import com.king.pos.Dao.StockRepository;
import com.king.pos.Dto.InventaireBordereauLigneUpdateRequest;
import com.king.pos.Dto.InventaireGenererBordereauxRequest;
import com.king.pos.Dto.Response.InventaireBordereauLigneResponse;
import com.king.pos.Dto.Response.InventaireBordereauResponse;
import com.king.pos.Entitys.Inventaire;
import com.king.pos.Entitys.InventaireBordereau;
import com.king.pos.Entitys.InventaireBordereauLigne;
import com.king.pos.Entitys.InventaireLigne;
import com.king.pos.Entitys.Locator;
import com.king.pos.Handllers.BusinessException;
import com.king.pos.enums.StatutBordereauInventaire;
import com.king.pos.enums.StatutInventaire;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventaireBordereauService {

    private final InventaireRepository inventaireRepository;
    private final InventaireLigneRepository inventaireLigneRepository;
    private final InventaireBordereauRepository bordereauRepository;
    private final InventaireBordereauLigneRepository bordereauLigneRepository;
    private final LocatorRepository locatorRepository;
    private final InventaireReferenceService referenceService;
    private final StockRepository stockRepository;
    private final StockAjustementInventaireService stockAjustementInventaireService;

    @Transactional
    public List<InventaireBordereauResponse> genererBordereaux(Long inventaireId, InventaireGenererBordereauxRequest request) {
        
        Inventaire inventaire = getInventaire(inventaireId);
        verifierInventaireModifiable(inventaire);

        if (inventaire.getStatut() == StatutInventaire.BROUILLON) {
            throw new BusinessException("Veuillez d'abord ouvrir l'inventaire.");
        }

        if (Boolean.TRUE.equals(inventaire.getBordereauxGeneres())) {
            throw new BusinessException("Les bordereaux ont déjà été générés pour cet inventaire.");
        }

        List<InventaireLigne> articles = inventaireLigneRepository.findByInventaireId(inventaireId);
        if (articles.isEmpty()) {
            throw new BusinessException("Aucune ligne d'inventaire à répartir.");
        }

        int taille = request.getTailleBordereau() != null && request.getTailleBordereau() > 0
                ? request.getTailleBordereau()
                : 50;

        boolean afficherQteTheo = Boolean.TRUE.equals(request.getAfficherQuantiteTheorique());

        articles.sort(
                Comparator.comparing((InventaireLigne a) -> a.getLocator() != null ? a.getLocator().getCode() : "")
                        .thenComparing(a -> a.getProduit() != null ? a.getProduit().getNom() : "")
        );

        List<InventaireBordereauResponse> responses = new ArrayList<>();
        int numeroOrdre = 1;

        for (int start = 0; start < articles.size(); start += taille) {
            int end = Math.min(start + taille, articles.size());
            List<InventaireLigne> bloc = articles.subList(start, end);

            Locator locator = null;
            boolean locatorUnique = bloc.stream()
                    .map(InventaireLigne::getLocator)
                    .distinct()
                    .count() == 1;
            if (locatorUnique) {
                locator = bloc.get(0).getLocator();
            }

            InventaireBordereau bordereau = InventaireBordereau.builder()
                    .reference(referenceService.nextBordereauReference())
                    .inventaire(inventaire)
                    .depot(inventaire.getDepot())
                    .locator(locator)
                    .numeroOrdre(numeroOrdre)
                    .tailleBordereau(taille)
                    .afficherQuantiteTheorique(afficherQteTheo)
                    .stockMisAJour(false)
                    .statut(StatutBordereauInventaire.BROUILLON)
                    .agentComptage(null)
                    .commentaire(null)
                    .build();

            bordereauRepository.save(bordereau);

            int numeroLigne = 1;
            List<InventaireBordereauLigne> lignes = new ArrayList<>();

            for (InventaireLigne article : bloc) {
                lignes.add(
                        InventaireBordereauLigne.builder()
                                .bordereau(bordereau)
                                .inventaireArticle(article)
                                .numeroLigne(numeroLigne++)
                                .quantiteComptee(null)
                                .commentaire(null)
                                .saisiPar(null)
                                .retenue(false)
                                .build()
                );
            }

            bordereauLigneRepository.saveAll(lignes);
            responses.add(mapBordereauToResponse(bordereau, lignes.size()));
            numeroOrdre++;
        }

        inventaire.setBordereauxGeneres(true);

        if (inventaire.getStatut() == StatutInventaire.OUVERT) {
            inventaire.setStatut(StatutInventaire.EN_COMPTAGE);
        }

        return responses;
    }


    
    private InventaireBordereauResponse mapBordereauToResponse(InventaireBordereau b, int nbLignes) {

     return InventaireBordereauResponse.builder()
                .id(b.getId())
                .reference(b.getReference())
                .inventaireId(b.getInventaire() != null ? b.getInventaire().getId() : null)
                .depotNom(b.getDepot() != null ? b.getDepot().getNom() : null)
                .locatorCode(b.getLocator() != null ? b.getLocator().getCode() : null)
                .numeroOrdre(b.getNumeroOrdre())
                .tailleBordereau(b.getTailleBordereau())
                .afficherQuantiteTheorique(b.getAfficherQuantiteTheorique())
                .stockMisAJour(b.getStockMisAJour())
                .statut(b.getStatut())
                .agentComptage(b.getAgentComptage())
                .commentaire(b.getCommentaire())
                .dateCreation(b.getDateCreation())
                .dateSaisie(b.getDateSaisie())
                .dateValidation(b.getDateValidation())
                .dateMiseAJourStock(b.getDateMiseAJourStock())
                .validePar(b.getValidePar())
                .agentComptage(b.getMisAJourStockPar())
                .tailleBordereau(b.getTailleBordereau())
                .nombreLignes(nbLignes)
                .build();
    }


@Transactional
public void saveLignes(Long bordereauId, List<InventaireBordereauLigneUpdateRequest> request) {
    InventaireBordereau bordereau = getBordereau(bordereauId);
    verifierInventaireModifiable(bordereau.getInventaire());

    if (bordereau.getStatut() == StatutBordereauInventaire.VALIDE ||
        bordereau.getStatut() == StatutBordereauInventaire.STOCK_MIS_A_JOUR) {
        throw new BusinessException("Ce bordereau ne peut plus être modifié.");
    }

    for (InventaireBordereauLigneUpdateRequest item : request) {
        InventaireBordereauLigne ligne = bordereauLigneRepository.findById(item.getId())
                .orElseThrow(() -> new BusinessException("Ligne bordereau introuvable"));

        if (!ligne.getBordereau().getId().equals(bordereauId)) {
            throw new BusinessException("La ligne n'appartient pas à ce bordereau.");
        }

        ligne.setQuantiteComptee(item.getQuantiteComptee());
        ligne.setCommentaire(item.getCommentaire());
        ligne.setSaisiPar(item.getSaisiPar());
    }

    bordereau.setStatut(StatutBordereauInventaire.SAISI);
    bordereau.setDateSaisie(LocalDateTime.now());
}

    @Transactional
    public void validerBordereau(Long bordereauId, String user) {
        InventaireBordereau bordereau = getBordereau(bordereauId);
        Inventaire inventaire = bordereau.getInventaire();

        verifierInventaireModifiable(inventaire);

        if (bordereau.getStatut() == StatutBordereauInventaire.STOCK_MIS_A_JOUR) {
            throw new BusinessException("Le stock a déjà été mis à jour sur ce bordereau.");
        }

        List<InventaireBordereauLigne> lignes = bordereauLigneRepository.findByBordereauId(bordereauId);
        if (lignes.isEmpty()) {
            throw new BusinessException("Le bordereau ne contient aucune ligne.");
        }

        boolean auMoinsUneLigneSaisie = lignes.stream().anyMatch(l -> l.getQuantiteComptee() != null);
        if (!auMoinsUneLigneSaisie) {
            throw new BusinessException("Aucune quantité comptée n'a été saisie pour ce bordereau.");
        }

        for (InventaireBordereauLigne ligne : lignes) {
            InventaireLigne invLigne = ligne.getInventaireArticle();

            bordereauLigneRepository.clearRetenueForInventaireArticle(invLigne.getId());

            ligne.setRetenue(true);
            ligne.setDateValidation(LocalDateTime.now());

            if (ligne.getQuantiteComptee() != null) {
                BigDecimal theorique = nvl(invLigne.getStockTheorique());
                BigDecimal physique = ligne.getQuantiteComptee();
                BigDecimal ecart = physique.subtract(theorique);
                BigDecimal valeur = ecart.multiply(nvl(invLigne.getPmp()));

                invLigne.setStockPhysiqueRetenu(physique);
                invLigne.setEcartQuantite(ecart);
                invLigne.setValeurEcart(valeur);
                invLigne.setCompte(true);
                invLigne.setDerniereDateComptage(LocalDateTime.now());
                invLigne.setDernierCommentaire(ligne.getCommentaire());
                invLigne.setDernierBordereauLigneValide(ligne);
            }
        }

        bordereau.setStatut(StatutBordereauInventaire.VALIDE);
        bordereau.setValidePar(user);
        bordereau.setDateValidation(LocalDateTime.now());
    }

@Transactional
public void miseAJourStock(Long bordereauId, String user) {
    InventaireBordereau bordereau = getBordereau(bordereauId);

    if (Boolean.TRUE.equals(bordereau.getStockMisAJour())) {
        throw new BusinessException("Le stock a déjà été mis à jour pour ce bordereau.");
    }

    if (bordereau.getStatut() != StatutBordereauInventaire.VALIDE) {
        throw new BusinessException("Veuillez d'abord valider le bordereau.");
    }

    List<InventaireBordereauLigne> lignes =
            bordereauLigneRepository.findByBordereauId(bordereauId);

    for (InventaireBordereauLigne ligneB : lignes) {
        InventaireLigne ligne = ligneB.getInventaireArticle();

        if (ligne == null) {
            continue;
        }

        if (!Boolean.TRUE.equals(ligne.getCompte())) {
            continue;
        }

        if (ligne.getProduit() == null || ligne.getProduit().getId() == null) {
            continue;
        }

        if (ligne.getDepot() == null || ligne.getDepot().getId() == null) {
            continue;
        }

        BigDecimal ecart = nvl(ligne.getEcartQuantite());

        if (ecart.compareTo(BigDecimal.ZERO) == 0) {
            continue;
        }

        BigDecimal tauxArticle = stockRepository
                .findByProduitIdAndDepotId(
                        ligne.getProduit().getId(),
                        ligne.getDepot().getId()
                )
                .map(stock -> nvl(stock.getTauxChangeUtilise()))
                .filter(taux -> taux.compareTo(BigDecimal.ZERO) > 0)
                .orElse(BigDecimal.ONE);

        stockAjustementInventaireService.ajusterDepuisInventaire(
                ligne.getProduit().getId(),
                ligne.getDepot().getId(),
                ligne.getLocator() != null ? ligne.getLocator().getId() : null,
                ligne.getStockLot() != null ? ligne.getStockLot().getId() : null,
                ecart,
                ligne.getPmp(),
                tauxArticle,
                bordereau.getReference()
        );
    }

    bordereau.setStockMisAJour(true);
    bordereau.setDateMiseAJourStock(LocalDateTime.now());
    bordereau.setMisAJourStockPar(user);
    bordereau.setStatut(StatutBordereauInventaire.STOCK_MIS_A_JOUR);
}

    private InventaireBordereauLigneResponse mapLigneToResponse(InventaireBordereauLigne l) {
        InventaireLigne a = l.getInventaireArticle();

        return InventaireBordereauLigneResponse.builder()
                .id(l.getId())
                .numeroLigne(l.getNumeroLigne())
                .inventaireArticleId(a != null ? a.getId() : null)
                .codeArticle(a != null && a.getProduit() != null ? a.getProduit().getCodeBarres() : null)
                .designation(a != null && a.getProduit() != null ? a.getProduit().getNom() : null)
                .depotNom(a != null && a.getDepot() != null ? a.getDepot().getNom() : null)
                .locatorCode(a != null && a.getLocator() != null ? a.getLocator().getCode() : null)
                .quantiteTheorique(a != null ? a.getStockTheorique() : null)
                .quantiteComptee(l.getQuantiteComptee())
                .commentaire(l.getCommentaire())
                .build();
    }

    private Inventaire getInventaire(Long id) {
        return inventaireRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Inventaire introuvable"));
    }

    private InventaireBordereau getBordereau(Long id) {
        return bordereauRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Bordereau introuvable"));
    }

    private void verifierInventaireModifiable(Inventaire inventaire) {
        if (inventaire.getStatut() == StatutInventaire.VALIDE ||
            inventaire.getStatut() == StatutInventaire.CLOTURE ||
            inventaire.getStatut() == StatutInventaire.ANNULE) {
            throw new BusinessException("Inventaire verrouillé. Modification impossible.");
        }
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }



       public List<InventaireBordereauResponse> getBordereauxByInventaire(Long inventaireId) {
        return bordereauRepository.findByInventaireId(inventaireId)
                .stream()
                .map(b -> mapBordereauToResponse(b, bordereauLigneRepository.findByBordereauId(b.getId()).size()))
                .toList();
    }



    public List<InventaireBordereauLigneResponse> getLignesByBordereau(Long bordereauId) {
        return bordereauLigneRepository.findByBordereauId(bordereauId)
                .stream()
                .map(this::mapLigneToResponse)
                .toList();
    }
}