package com.king.pos.ImplementServices;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.king.pos.Dao.DepotRepository;
import com.king.pos.Dao.InventaireBordereauRepository;
import com.king.pos.Dao.InventaireLigneRepository;
import com.king.pos.Dao.InventaireRepository;
import com.king.pos.Dao.LocatorRepository;
import com.king.pos.Dao.ProduitLocatorRepository;
import com.king.pos.Dao.StockLotRepository;
import com.king.pos.Dao.StockRepository;
import com.king.pos.Dto.InventaireCreateRequest;
import com.king.pos.Dto.Response.InventaireArticleResponse;
import com.king.pos.Dto.Response.InventaireResponse;
import com.king.pos.Entitys.Depot;
import com.king.pos.Entitys.Inventaire;
import com.king.pos.Entitys.InventaireLigne;
import com.king.pos.Entitys.Locator;
import com.king.pos.Entitys.ProduitLocator;
import com.king.pos.Entitys.StockLot;
import com.king.pos.Entitys.StockProduit;
import com.king.pos.Handllers.BusinessException;
import com.king.pos.enums.StatutBordereauInventaire;
import com.king.pos.enums.StatutInventaire;
import com.king.pos.enums.TypeInventaire;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InventaireService {
private final InventaireRepository inventaireRepository;
    private final InventaireLigneRepository inventaireLigneRepository;
    private final DepotRepository depotRepository;
    private final LocatorRepository locatorRepository;
    private final StockRepository stockProduitRepository;
    private final StockLotRepository stockLotRepository;
    private final InventaireReferenceService referenceService;
    private final ProduitLocatorRepository produitLocatorRepository;
    private final InventaireBordereauRepository bordereauRepository;


    public InventaireResponse create(InventaireCreateRequest request) {
        Depot depot = depotRepository.findById(request.getDepotId())
                .orElseThrow(() -> new BusinessException("Dépôt introuvable"));

        Locator locator = null;

        if (request.getType() == TypeInventaire.CIBLE && request.getLocatorId() != null) {
            locator = locatorRepository.findById(request.getLocatorId())
                    .orElseThrow(() -> new BusinessException("Locator introuvable"));
        }

        Inventaire inventaire = Inventaire.builder()
                .reference(referenceService.nextInventaireReference())
                .type(request.getType())
                .statut(StatutInventaire.BROUILLON)
                .depot(depot)
                .locator(locator)
                .dateInventaire(request.getDateInventaire())
                .creePar(request.getCreePar())
                .commentaire(request.getCommentaire())
                .memorise(request.getMemorise() != null ? request.getMemorise() : true)
                .gelStockTheorique(request.getGelStockTheorique() != null ? request.getGelStockTheorique() : true)
                .annule(false)
                .bordereauxGeneres(false)
                .build();

        inventaireRepository.save(inventaire);
        return mapToResponse(inventaire);
    }

    

@Transactional
public InventaireResponse ouvrir(Long inventaireId) {

    Inventaire inventaire = getInventaire(inventaireId);

    if (inventaire.getStatut() != StatutInventaire.BROUILLON) {
        throw new BusinessException("Seul un inventaire en brouillon peut être ouvert.");
    }

    if (inventaireLigneRepository.countByInventaireId(inventaireId) > 0) {
        throw new BusinessException("Les lignes de cet inventaire sont déjà générées.");
    }

    List<InventaireLigne> articles = genererArticlesDepuisStock(inventaire);

    if (articles.isEmpty()) {
        throw new BusinessException("Aucun article trouvé pour cet inventaire.");
    }

    inventaireLigneRepository.saveAll(articles);

    inventaire.setDateOuverture(LocalDateTime.now());
    inventaire.setStatut(StatutInventaire.OUVERT);

    // état métier initial après ouverture
    inventaire.setBordereauxGeneres(false);
    inventaire.setVarianceLancee(false);
    inventaire.setValide(false);
    inventaire.setCloture(false);
    inventaire.setAnnule(false);

    // dates / utilisateurs finaux remis à zéro
    inventaire.setDateValidation(null);
    inventaire.setDateCloture(null);
    inventaire.setDateAnnulation(null);

    inventaire.setValidePar(null);
    inventaire.setCloturePar(null);
    inventaire.setAnnulePar(null);

    inventaire.setCommentaireAnnulation(null);

    inventaireRepository.save(inventaire);

    return mapToResponse(inventaire);
}

   private List<InventaireLigne> genererArticlesDepuisStock(Inventaire inventaire) {
    List<InventaireLigne> result = new ArrayList<>();

    Long depotId = inventaire.getDepot().getId();
    Long locatorFiltreId = inventaire.getLocator() != null
            ? inventaire.getLocator().getId()
            : null;

    List<StockProduit> stocks = stockProduitRepository.findByDepotId(depotId);
    List<ProduitLocator> produitLocators = produitLocatorRepository.findByDepotId(depotId);

    Map<Long, Locator> locatorParProduit = produitLocators.stream()
            .filter(pl -> pl.getProduit() != null && pl.getProduit().getId() != null)
            .filter(pl -> pl.getLocator() != null && pl.getLocator().getId() != null)
            .collect(Collectors.toMap(
                    pl -> pl.getProduit().getId(),
                    ProduitLocator::getLocator,
                    (l1, l2) -> l1
            ));

    for (StockProduit sp : stocks) {

        if (sp.getProduit() == null || sp.getProduit().getId() == null) {
            continue;
        }

        Locator locator = locatorParProduit.get(sp.getProduit().getId());

        if (locatorFiltreId != null) {
            if (locator == null || !locatorFiltreId.equals(locator.getId())) {
                continue;
            }
        }

        if (nvl(sp.getQuantiteDisponible()).compareTo(BigDecimal.ZERO) <= 0) {
            continue;
        }

        result.add(
                InventaireLigne.builder()
                        .inventaire(inventaire)
                        .produit(sp.getProduit())
                        .depot(inventaire.getDepot())
                        .locator(locator)

                        // IMPORTANT : STOCK THEORIQUE = STOCK PRODUIT
                        .stockTheorique(nvl(sp.getQuantiteDisponible()))

                        .stockLot(null)
                        .stockPhysiqueRetenu(null)
                        .ecartQuantite(null)
                        .pmp(nvl(sp.getPmp()))
                        .valeurEcart(null)
                        .compte(false)
                        .varianceGeneree(false)
                        .build()
        );
    }

    return result;
}

    @Transactional
    public List<InventaireResponse> getAll() {
        return inventaireRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public InventaireResponse getById(Long id) {
        return mapToResponse(getInventaire(id));
    }

    @Transactional
    public List<InventaireArticleResponse> getArticlesByInventaire(Long inventaireId) {
        return inventaireLigneRepository.findByInventaireId(inventaireId)
                .stream()
                .map(this::mapArticleToResponse)
                .toList();
    }

    public Inventaire getInventaire(Long id) {
        return inventaireRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Inventaire introuvable"));
    }

    private InventaireArticleResponse mapArticleToResponse(InventaireLigne a) {
        return InventaireArticleResponse.builder()
                .id(a.getId())
                .produitId(a.getProduit() != null ? a.getProduit().getId() : null)
                .codeArticle(a.getProduit() != null ? a.getProduit().getCodeBarres() : null)
                .designation(a.getProduit() != null ? a.getProduit().getNom() : null)
                .depotNom(a.getDepot() != null ? a.getDepot().getNom() : null)
                .locatorCode(a.getLocator() != null ? a.getLocator().getCode() : null)
                .stockTheorique(a.getStockTheorique())
                .stockPhysiqueRetenu(a.getStockPhysiqueRetenu())
                .ecartQuantite(a.getEcartQuantite())
                .valeurEcart(a.getValeurEcart())
                .compte(Boolean.TRUE.equals(a.getCompte()))
                .dernierCommentaire(a.getDernierCommentaire())
                .derniereDateComptage(a.getDerniereDateComptage())
                .build();
    }

    private boolean sameLocatorId(Locator a, Locator b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.getId().equals(b.getId());
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

 private InventaireResponse mapToResponse(Inventaire i) {

    Boolean tousBordereauxStockMisAJour = null;

    if (i.getId() != null) {
        boolean existeBordereauNonMisAJour =
                bordereauRepository.existsByInventaireIdAndStatutNot(
                        i.getId(),
                        StatutBordereauInventaire.STOCK_MIS_A_JOUR
                );

        tousBordereauxStockMisAJour = !existeBordereauNonMisAJour;
    }

    return InventaireResponse.builder()
            .id(i.getId())
            .reference(i.getReference())
            .type(i.getType())
            .statut(i.getStatut())
            .depotId(i.getDepot() != null ? i.getDepot().getId() : null)
            .depotNom(i.getDepot() != null ? i.getDepot().getNom() : null)
            .locatorId(i.getLocator() != null ? i.getLocator().getId() : null)
            .locatorCode(i.getLocator() != null ? i.getLocator().getCode() : null)
            .dateInventaire(i.getDateInventaire())
            .dateOuverture(i.getDateOuverture())
            .dateValidation(i.getDateValidation())
            .dateCloture(i.getDateCloture())
            .memorise(i.getMemorise())
            .gelStockTheorique(i.getGelStockTheorique())
            .varianceLancee(i.getVarianceLancee())
            .valide(i.getValide())
            .cloture(i.getCloture())
            .commentaire(i.getCommentaire())

            // important
            .tousBordereauxStockMisAJour(tousBordereauxStockMisAJour)

            .build();
}
}