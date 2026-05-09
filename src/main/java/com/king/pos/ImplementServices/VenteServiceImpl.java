package com.king.pos.ImplementServices;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.king.pos.Dao.ProduitRepository;
import com.king.pos.Dao.VenteLigneRepository;
import com.king.pos.Dao.VenteRepository;
import com.king.pos.Dto.AnnulationVenteRequest;
import com.king.pos.Dto.LigneVenteRequest;
import com.king.pos.Dto.LotConsommationResult;
import com.king.pos.Dto.RapportVenteDetailResponse;
import com.king.pos.Dto.RapportVenteFilterRequest;
import com.king.pos.Dto.RapportVenteKpiResponse;
import com.king.pos.Dto.RapportVentePosResponse;
import com.king.pos.Dto.VenteRequest;
import com.king.pos.Dto.Response.VenteLigneResponse;
import com.king.pos.Dto.Response.VenteResponse;
import com.king.pos.Entitys.LigneVente;
import com.king.pos.Entitys.Produit;
import com.king.pos.Entitys.Vente;
import com.king.pos.Entitys.VenteLotConsommation;
import com.king.pos.Handllers.BusinessException;
import com.king.pos.Handllers.ResourceNotFoundException;
import com.king.pos.Interface.VenteService;
import com.king.pos.enums.StatutVente;
import com.king.pos.enums.TypeMouvementStock;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.king.pos.Dao.DepotRepository;
import com.king.pos.Dto.TransactionStockRequest;
import com.king.pos.Dto.TypeRequisition;
import com.king.pos.Entitys.Depot;

@Service
@RequiredArgsConstructor
@Transactional
public class VenteServiceImpl implements VenteService {

    private final VenteRepository venteRepository;
    private final ProduitRepository produitRepository;
    private final DepotRepository depotRepository;
    private final TransactionStockService transactionStockService;
    private final SortieStockLotService sortieStockLotService;
    private final VenteLotConsommationService venteLotConsommationService;
    private final VenteLigneRepository ligneVenteRepository;
    private final RequisitionService requisitionService;


@Override
public VenteResponse enregistrerVente(VenteRequest request) {
    validateRequest(request);

    Depot depot = depotRepository.findById(request.getDepotId())
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Dépôt introuvable : " + request.getDepotId()));

    BigDecimal taux = safeTaux(request.getTauxChange());

    Vente vente = Vente.builder()
            .ticketNumero(trimToNull(request.getTicketNumero()))
            .clientNom(hasText(request.getClientNom())
                    ? request.getClientNom().trim()
                    : "CLIENT DIVERS")
            .caissier(trimToNull(request.getCaissier()))
            .modePaiement(request.getModePaiement())
            .devise(request.getDevise())
            .tauxChange(taux)

            .montantRecu(nvl(request.getMontantRecu()))
            .monnaie(nvl(request.getMonnaie()))

            .sousTotalCDF(nvl(request.getSousTotalCDF()))
            .totalRemiseCDF(nvl(request.getTotalRemiseCDF()))
            .totalGeneralCDF(nvl(request.getTotalGeneralCDF()))
            .montantRecuCDF(nvl(request.getMontantRecuCDF()))
            .monnaieCDF(nvl(request.getMonnaieCDF()))

            .pmpCDF(nvl(request.getPmpCDF()))
            .totalPmpCDF(nvl(request.getTotalPmpCDF()))
            .margeCDF(nvl(request.getMargeCDF()))

            .sousTotalUSD(toUSD(nvl(request.getSousTotalCDF()), taux))
            .totalRemiseUSD(toUSD(nvl(request.getTotalRemiseCDF()), taux))
            .totalGeneralUSD(toUSD(nvl(request.getTotalGeneralCDF()), taux))
            .montantRecuUSD(toUSD(nvl(request.getMontantRecuCDF()), taux))
            .monnaieUSD(toUSD(nvl(request.getMonnaieCDF()), taux))

            .pmpUSD(toUSD(nvl(request.getPmpCDF()), taux))
            .totalPmpUSD(toUSD(nvl(request.getTotalPmpCDF()), taux))
            .margeUSD(toUSD(nvl(request.getMargeCDF()), taux))

            .totalHT(nvl(request.getSousTotal()))
            .totalRemise(nvl(request.getTotalRemise()))
            .totalTTC(nvl(request.getTotalGeneral()))

            .statut(StatutVente.VALIDE)
            .depot(depot)
            .build();

    List<LigneVente> lignes = new ArrayList<>();

    for (LigneVenteRequest lr : request.getLignes()) {

        Produit produit = produitRepository.findById(lr.getProduitId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produit introuvable : " + lr.getProduitId()));

        if (!Boolean.TRUE.equals(produit.getActif())) {
            throw new BusinessException("Produit inactif : " + produit.getNom());
        }

        LigneVente ligne = LigneVente.builder()
                .vente(vente)
                .produit(produit)
                .quantite(nvl(lr.getQuantite()))

                .prixUnitaire(nvl(lr.getPrix()))
                .remise(nvl(lr.getRemise()))
                .sousTotal(nvl(lr.getTotal()))

                .prixCDF(nvl(lr.getPrixCDF()))
                .remiseCDF(nvl(lr.getRemiseCDF()))
                .totalCDF(nvl(lr.getTotalCDF()))

                .pmpCDF(nvl(lr.getPmpCDF()))
                .totalPmpCDF(nvl(lr.getTotalPmpCDF()))
                .margeCDF(nvl(lr.getMargeCDF()))

                .prixUSD(toUSD(nvl(lr.getPrixCDF()), taux))
                .remiseUSD(toUSD(nvl(lr.getRemiseCDF()), taux))
                .totalUSD(toUSD(nvl(lr.getTotalCDF()), taux))
                .pmpUSD(toUSD(nvl(lr.getPmpCDF()), taux))
                .totalPmpUSD(toUSD(nvl(lr.getTotalPmpCDF()), taux))
                .margeUSD(toUSD(nvl(lr.getMargeCDF()), taux))

                .tauxChange(taux)
                .build();

        lignes.add(ligne);
    }

    vente.setLignes(lignes);

    Vente saved = venteRepository.save(vente);

    enregistrerRequisitionVenteReelle(saved);

    appliquerSortieStock(saved);

    return mapToResponse(saved);
}


    private void enregistrerRequisitionVenteReelle(Vente vente) {
        if (vente == null || vente.getLignes() == null) {
            return;
        }

        for (LigneVente ligne : vente.getLignes()) {
            if (ligne.getProduit() == null) {
                continue;
            }

            Long depotId = vente.getDepot() != null ? vente.getDepot().getId() : null;

            Long locatorId = null;

            // if (ligne.getLocator() != null) {
            // locatorId = ligne.getLocator().getId();
            // }

            requisitionService.enregistrerDemande(
                    ligne.getProduit().getId(),
                    depotId,
                    locatorId,
                    nvl(ligne.getQuantite()),
                    TypeRequisition.VENTE_REALISEE,
                    vente.getCaissier());
        }
    }

    private void appliquerSortieStock(Vente saved) {
        for (LigneVente ligne : saved.getLignes()) {

            BigDecimal quantiteVendue = nvl(ligne.getQuantite());

            List<LotConsommationResult> consommations = sortieStockLotService.consommerEnFefo(
                    ligne.getProduit(),
                    saved.getDepot(),
                    quantiteVendue);

            venteLotConsommationService.enregistrerConsommations(
                    saved,
                    ligne,
                    consommations);

            transactionStockService.appliquerTransaction(
                    TransactionStockRequest.builder()
                            .typeTransaction(TypeMouvementStock.VENTE_SORTIE)
                            .produit(ligne.getProduit())
                            .depot(saved.getDepot())
                            .quantite(quantiteVendue)
                            .prixUnitaire(nvl(ligne.getPrixUnitaire()))
                            .fraisUnitaire(BigDecimal.ZERO)
                            .coutUnitaireFinal(BigDecimal.ZERO)
                            .referenceDocument(saved.getTicketNumero())
                            .sourceDocument("VENTE")
                            .sourceDocumentId(saved.getId())
                            .libelle("Sortie stock après vente POS - Ticket " + saved.getTicketNumero())
                            .utilisateur(saved.getCaissier())
                            .build());
        }
    }

    @Override
    public List<VenteResponse> getAllVente() {
        return venteRepository.findAll(Sort.by(Sort.Direction.DESC, "dateVente"))
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
@Override
@Transactional
public VenteResponse annulerVente(Long venteId, AnnulationVenteRequest request) {

    if (venteId == null) {
        throw new BusinessException("Le numéro de vente est obligatoire.");
    }

    Vente venteOriginale = venteRepository.findById(venteId)
            .orElseThrow(() -> new ResourceNotFoundException("Vente introuvable : " + venteId));

    if (request == null || request.getCommentaire() == null || request.getCommentaire().trim().isEmpty()) {
        throw new BusinessException("Le commentaire d'annulation est obligatoire.");
    }

    if (venteOriginale.getLignes() == null || venteOriginale.getLignes().isEmpty()) {
        throw new BusinessException("Impossible d'annuler une vente sans lignes.");
    }

    if (venteOriginale.getDepot() == null) {
        throw new BusinessException("Aucun dépôt n'est associé à cette vente.");
    }

    if (venteRepository.existsByVenteOrigineId(venteOriginale.getId())) {
        throw new BusinessException("Un retour a déjà été généré pour cette vente.");
    }

    List<VenteLotConsommation> consommations =
            venteLotConsommationService.getByVenteId(venteOriginale.getId());

    if (consommations.isEmpty()) {
        throw new BusinessException(
                "Retour de vente impossible : aucune traçabilité de lots n'a été trouvée pour cette vente.");
    }

    sortieStockLotService.remettreEnStockLotsAnnules(consommations);

    for (LigneVente ligne : venteOriginale.getLignes()) {
        BigDecimal quantite = nvl(ligne.getQuantite());

        transactionStockService.appliquerTransaction(
                TransactionStockRequest.builder()
                        .typeTransaction(TypeMouvementStock.ANNULATION_VENTE_ENTREE)
                        .produit(ligne.getProduit())
                        .depot(venteOriginale.getDepot())
                        .quantite(quantite)
                        .prixUnitaire(nvl(ligne.getPrixUnitaire()))
                        .fraisUnitaire(BigDecimal.ZERO)
                        .coutUnitaireFinal(nvl(ligne.getPrixUnitaire()))
                        .referenceDocument(venteOriginale.getTicketNumero())
                        .sourceDocument("RETOUR_VENTE")
                        .sourceDocumentId(venteOriginale.getId())
                        .libelle("Retour stock après annulation vente - Ticket " + venteOriginale.getTicketNumero())
                        .utilisateur(venteOriginale.getCaissier())
                        .build()
        );
    }

    BigDecimal taux = safeTaux(venteOriginale.getTauxChange());

    Vente retour = Vente.builder()
            .dateVente(LocalDateTime.now())
            .ticketNumero(genererNumeroRetour(venteOriginale.getTicketNumero()))
            .clientNom(venteOriginale.getClientNom())
            .caissier(venteOriginale.getCaissier())
            .depot(venteOriginale.getDepot())
            .devise(venteOriginale.getDevise())
            .tauxChange(taux)
            .modePaiement(venteOriginale.getModePaiement())
            .statut(StatutVente.RETOURNEE)
            .venteOrigine(venteOriginale)
            .commentaireAnnulation(request.getCommentaire().trim())

            .totalHT(nvl(venteOriginale.getTotalHT()).negate())
            .totalRemise(nvl(venteOriginale.getTotalRemise()).negate())
            .totalTTC(nvl(venteOriginale.getTotalTTC()).negate())
            .montantRecu(nvl(venteOriginale.getMontantRecu()).negate())
            .monnaie(nvl(venteOriginale.getMonnaie()).negate())

            .sousTotalCDF(nvl(venteOriginale.getSousTotalCDF()).negate())
            .totalRemiseCDF(nvl(venteOriginale.getTotalRemiseCDF()).negate())
            .totalGeneralCDF(nvl(venteOriginale.getTotalGeneralCDF()).negate())
            .montantRecuCDF(nvl(venteOriginale.getMontantRecuCDF()).negate())
            .monnaieCDF(nvl(venteOriginale.getMonnaieCDF()).negate())

            .pmpCDF(nvl(venteOriginale.getPmpCDF()).negate())
            .totalPmpCDF(nvl(venteOriginale.getTotalPmpCDF()).negate())
            .margeCDF(nvl(venteOriginale.getMargeCDF()).negate())

            .sousTotalUSD(nvl(venteOriginale.getSousTotalUSD()).negate())
            .totalRemiseUSD(nvl(venteOriginale.getTotalRemiseUSD()).negate())
            .totalGeneralUSD(nvl(venteOriginale.getTotalGeneralUSD()).negate())
            .montantRecuUSD(nvl(venteOriginale.getMontantRecuUSD()).negate())
            .monnaieUSD(nvl(venteOriginale.getMonnaieUSD()).negate())

            .pmpUSD(nvl(venteOriginale.getPmpUSD()).negate())
            .totalPmpUSD(nvl(venteOriginale.getTotalPmpUSD()).negate())
            .margeUSD(nvl(venteOriginale.getMargeUSD()).negate())

            .build();

    Vente retourSaved = venteRepository.save(retour);

    List<LigneVente> lignesRetour = new ArrayList<>();

    for (LigneVente ligneOriginale : venteOriginale.getLignes()) {

        LigneVente ligneRetour = LigneVente.builder()
                .vente(retourSaved)
                .produit(ligneOriginale.getProduit())
                .tarifVente(ligneOriginale.getTarifVente())

                .quantite(nvl(ligneOriginale.getQuantite()).negate())
                .prixUnitaire(nvl(ligneOriginale.getPrixUnitaire()))
                .remise(nvl(ligneOriginale.getRemise()).negate())
                .sousTotal(nvl(ligneOriginale.getSousTotal()).negate())

                .prixCDF(nvl(ligneOriginale.getPrixCDF()))
                .remiseCDF(nvl(ligneOriginale.getRemiseCDF()).negate())
                .totalCDF(nvl(ligneOriginale.getTotalCDF()).negate())

                .pmpCDF(nvl(ligneOriginale.getPmpCDF()))
                .totalPmpCDF(nvl(ligneOriginale.getTotalPmpCDF()).negate())
                .margeCDF(nvl(ligneOriginale.getMargeCDF()).negate())

                .prixUSD(nvl(ligneOriginale.getPrixUSD()))
                .remiseUSD(nvl(ligneOriginale.getRemiseUSD()).negate())
                .totalUSD(nvl(ligneOriginale.getTotalUSD()).negate())

                .pmpUSD(nvl(ligneOriginale.getPmpUSD()))
                .totalPmpUSD(nvl(ligneOriginale.getTotalPmpUSD()).negate())
                .margeUSD(nvl(ligneOriginale.getMargeUSD()).negate())

                .tauxChange(taux)

                .pmpAuMomentVente(nvl(ligneOriginale.getPmpAuMomentVente()))
                .tauxMarge(nvl(ligneOriginale.getTauxMarge()))
                .tauxRemiseMax(nvl(ligneOriginale.getTauxRemiseMax()))
                .tauxRemiseAppliquee(nvl(ligneOriginale.getTauxRemiseAppliquee()))
                .prixBrut(nvl(ligneOriginale.getPrixBrut()))
                .montantRemise(nvl(ligneOriginale.getMontantRemise()).negate())
                .prixUnitaireVente(nvl(ligneOriginale.getPrixUnitaireVente()))
                .tauxTva(nvl(ligneOriginale.getTauxTva()))

                .build();

        LigneVente ligneRetourSaved = ligneVenteRepository.save(ligneRetour);
        lignesRetour.add(ligneRetourSaved);

        List<VenteLotConsommation> consommationsLigneOriginale = consommations.stream()
                .filter(c -> c.getLigneVente() != null
                        && c.getLigneVente().getId() != null
                        && c.getLigneVente().getId().equals(ligneOriginale.getId()))
                .toList();

        if (!consommationsLigneOriginale.isEmpty()) {
            venteLotConsommationService.enregistrerConsommationsRetour(
                    retourSaved,
                    ligneRetourSaved,
                    consommationsLigneOriginale
            );
        }
    }

    retourSaved.setLignes(lignesRetour);

    venteOriginale.setStatut(StatutVente.ANNULEE);
    venteRepository.save(venteOriginale);

    return mapToResponse(retourSaved);
}
    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String genererNumeroRetour(String ticketOrigine) {
        String base = (ticketOrigine != null && !ticketOrigine.isBlank()) ? ticketOrigine : "VENTE";
        return "RET-" + base + "-" + System.currentTimeMillis();
    }

    private VenteResponse mapToResponse(Vente vente) {
        List<VenteLigneResponse> lignes = new ArrayList<>();

        if (vente.getLignes() != null) {
            for (LigneVente ligne : vente.getLignes()) {
                Produit produit = ligne.getProduit();

                lignes.add(VenteLigneResponse.builder()
                        .produitId(produit != null ? produit.getId() : null)
                        .produitNom(produit != null ? produit.getNom() : "NP")
                        .quantite(nvl(ligne.getQuantite()))
                        .prixUnitaire(nvl(ligne.getPrixUnitaire()))
                        .remise(nvl(ligne.getRemise()))
                        .totalLigne(nvl(ligne.getSousTotal()))

                        .prixCDF(nvl(ligne.getPrixCDF()))
                        .remiseCDF(nvl(ligne.getRemiseCDF()))
                        .totalCDF(nvl(ligne.getTotalCDF()))

                        .prixUSD(nvl(ligne.getPrixUSD()))
                        .remiseUSD(nvl(ligne.getRemiseUSD()))
                        .totalUSD(nvl(ligne.getTotalUSD()))

                        .pmpCDF(nvl(ligne.getPmpCDF()))
                        .totalPmpCDF(nvl(ligne.getTotalPmpCDF()))
                        .margeCDF(nvl(ligne.getMargeCDF()))

                        .pmpUSD(nvl(ligne.getPmpUSD()))
                        .totalPmpUSD(nvl(ligne.getTotalPmpUSD()))
                        .margeUSD(nvl(ligne.getMargeUSD()))
                        .build());
            }
        }

        return VenteResponse.builder()
                .id(vente.getId())
                .ticketNumero(vente.getTicketNumero())
                .dateVente(vente.getDateVente())
                .clientNom(hasText(vente.getClientNom()) ? vente.getClientNom() : "CLIENT DIVERS")
                .caissier(vente.getCaissier())
                .modePaiement(vente.getModePaiement() != null ? vente.getModePaiement().name() : null)

                .devise(vente.getDevise() != null ? vente.getDevise() : "CDF")
                .tauxChange(nvl(vente.getTauxChange()))

                .montantRecu(nvl(vente.getMontantRecu()))
                .monnaie(nvl(vente.getMonnaie()))
                .sousTotal(nvl(vente.getTotalHT()))
                .totalRemise(nvl(vente.getTotalRemise()))
                .totalGeneral(nvl(vente.getTotalTTC()))

                .sousTotalCDF(nvl(vente.getSousTotalCDF()))
                .totalRemiseCDF(nvl(vente.getTotalRemiseCDF()))
                .totalGeneralCDF(nvl(vente.getTotalGeneralCDF()))
                .montantRecuCDF(nvl(vente.getMontantRecuCDF()))
                .monnaieCDF(nvl(vente.getMonnaieCDF()))

                .pmpCDF(nvl(vente.getPmpCDF()))
                .totalPmpCDF(nvl(vente.getTotalPmpCDF()))
                .margeCDF(nvl(vente.getMargeCDF()))

                .sousTotalUSD(nvl(vente.getSousTotalUSD()))
                .totalRemiseUSD(nvl(vente.getTotalRemiseUSD()))
                .totalGeneralUSD(nvl(vente.getTotalGeneralUSD()))
                .montantRecuUSD(nvl(vente.getMontantRecuUSD()))
                .monnaieUSD(nvl(vente.getMonnaieUSD()))

                .pmpUSD(nvl(vente.getPmpUSD()))
                .totalPmpUSD(nvl(vente.getTotalPmpUSD()))
                .margeUSD(nvl(vente.getMargeUSD()))

                .tarifId(vente.getTarif() != null ? vente.getTarif().getId() : null)
                .depotId(vente.getDepot() != null ? vente.getDepot().getId() : null)

                .statut(vente.getStatut() != null ? vente.getStatut().name() : null)
                .lignes(lignes)
                .build();
    }

    private void validateRequest(VenteRequest request) {
        if (request == null) {
            throw new BusinessException("La requête de vente est invalide");
        }

        if (request.getLignes() == null || request.getLignes().isEmpty()) {
            throw new BusinessException("La vente doit contenir au moins une ligne");
        }

        if (request.getModePaiement() == null) {
            throw new BusinessException("Le mode de paiement est obligatoire");
        }

        if (request.getDepotId() == null) {
            throw new BusinessException("Le dépôt est obligatoire");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

@Transactional
public RapportVentePosResponse genererRapportVentes(RapportVenteFilterRequest filter) {

    String caissier = hasText(filter.getCaissier()) ? filter.getCaissier().trim() : null;
    String devise = hasText(filter.getDevise()) ? filter.getDevise().trim() : null;

    List<Vente> ventes = venteRepository.findRapportVentes(
            filter.getDateDebut(),
            filter.getDateFin(),
            filter.getDepotId(),
            caissier,
            devise
    );

    List<RapportVenteDetailResponse> details = new ArrayList<>();
    java.util.Map<String, List<RapportVenteDetailResponse>> grouped = new HashMap<>();

    for (Vente vente : ventes) {

        BigDecimal tauxChange = safeTaux(vente.getTauxChange());

        for (LigneVente ligne : vente.getLignes()) {

            BigDecimal quantite = nvl(ligne.getQuantite());

            BigDecimal prixBrutCDF = nvl(ligne.getPrixCDF());
            if (prixBrutCDF.compareTo(BigDecimal.ZERO) == 0) {
                prixBrutCDF = nvl(ligne.getPrixUnitaire());
            }

            BigDecimal remiseCDF = nvl(ligne.getRemiseCDF());
            if (remiseCDF.compareTo(BigDecimal.ZERO) == 0) {
                remiseCDF = nvl(ligne.getRemise());
            }

            BigDecimal totalNetCDF = nvl(ligne.getTotalCDF());
            if (totalNetCDF.compareTo(BigDecimal.ZERO) == 0) {
                totalNetCDF = nvl(ligne.getSousTotal());
            }

            BigDecimal prixNetCDF = prixBrutCDF.subtract(remiseCDF);

            BigDecimal pmpCDF = nvl(ligne.getPmpCDF());
            BigDecimal totalPmpCDF = nvl(ligne.getTotalPmpCDF());

            if (totalPmpCDF.compareTo(BigDecimal.ZERO) == 0) {
                totalPmpCDF = pmpCDF.multiply(quantite);
            }

            BigDecimal margeCDF = nvl(ligne.getMargeCDF());
            if (margeCDF.compareTo(BigDecimal.ZERO) == 0) {
                margeCDF = totalNetCDF.subtract(totalPmpCDF);
            }

            BigDecimal prixBrutUSD = toUSD(prixBrutCDF, tauxChange);
            BigDecimal remiseUSD = toUSD(remiseCDF, tauxChange);
            BigDecimal prixNetUSD = toUSD(prixNetCDF, tauxChange);
            BigDecimal totalNetUSD = toUSD(totalNetCDF, tauxChange);

            BigDecimal pmpUSD = toUSD(pmpCDF, tauxChange);
            BigDecimal totalPmpUSD = toUSD(totalPmpCDF, tauxChange);
            BigDecimal margeUSD = toUSD(margeCDF, tauxChange);

            BigDecimal pourcentageMarge = BigDecimal.ZERO;

            if (totalNetCDF.compareTo(BigDecimal.ZERO) > 0) {
                pourcentageMarge = margeCDF.multiply(BigDecimal.valueOf(100))
                        .divide(totalNetCDF, 2, RoundingMode.HALF_UP);
            }

            String cst = ligne.getProduit().getCategorie() != null
                    ? ligne.getProduit().getCategorie().getId().toString()
                    : "N/A";

            RapportVenteDetailResponse detail = RapportVenteDetailResponse.builder()
                    .succursale(vente.getDepot().getNom())
                    .serviceCredite("POS")
                    .module("VENTE_POS")
                    .natureOperation(vente.getStatut().name())

                    .numeroCC(vente.getTicketNumero())
                    .dateCC(vente.getDateVente())

                    .typeCommandeOuOR(vente.getModePaiement().name())
                    .libelleType("VENTE POS")

                    .nomClient(vente.getClientNom())
                    .tarif(ligne.getTarifVente() != null ? ligne.getTarifVente().getCode() : null)
                    .operateur(vente.getCaissier())

                    .quantiteCommandee(quantite)
                    .quantiteFacturee(quantite)

                    .numeroFacture(vente.getTicketNumero())
                    .dateFacture(vente.getDateVente())

                    .cst(cst)
                    .reference(ligne.getProduit().getCodeBarres())
                    .designation(ligne.getProduit().getNom())

                    .coursDevise(tauxChange)

                    .prixBrut(prixBrutCDF)
                    .remise(remiseCDF)
                    .prixNet(prixNetCDF)
                    .pmp(pmpCDF)
                    .totalNet(totalNetCDF)
                    .totalPmp(totalPmpCDF)
                    .marge(margeCDF)

                    .prixBrutCDF(prixBrutCDF)
                    .prixBrutUSD(prixBrutUSD)

                    .remiseCDF(remiseCDF)
                    .remiseUSD(remiseUSD)

                    .prixNetCDF(prixNetCDF)
                    .prixNetUSD(prixNetUSD)

                    .pmpCDF(pmpCDF)
                    .pmpUSD(pmpUSD)

                    .totalNetCDF(totalNetCDF)
                    .totalNetUSD(totalNetUSD)

                    .totalPmpCDF(totalPmpCDF)
                    .totalPmpUSD(totalPmpUSD)

                    .margeCDF(margeCDF)
                    .margeUSD(margeUSD)

                    .pourcentageMarge(pourcentageMarge)

                    .tauxTva(nvl(ligne.getTauxTva()))
                    .totalTtc(totalNetCDF)
                    .totalTtcCDF(totalNetCDF)
                    .totalTtcUSD(totalNetUSD)

                    .build();

            details.add(detail);
            grouped.computeIfAbsent(cst, k -> new ArrayList<>()).add(detail);
        }
    }

    List<RapportVenteKpiResponse> kpis = new ArrayList<>();

    for (var entry : grouped.entrySet()) {

        String cst = entry.getKey();
        List<RapportVenteDetailResponse> lignes = entry.getValue();

        BigDecimal totalNetCDF = lignes.stream()
                .map(x -> nvl(x.getTotalNetCDF()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPmpCDF = lignes.stream()
                .map(x -> nvl(x.getTotalPmpCDF()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal margeCDF = totalNetCDF.subtract(totalPmpCDF);

        BigDecimal totalNetUSD = lignes.stream()
                .map(x -> nvl(x.getTotalNetUSD()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPmpUSD = lignes.stream()
                .map(x -> nvl(x.getTotalPmpUSD()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal margeUSD = totalNetUSD.subtract(totalPmpUSD);

        BigDecimal pct = BigDecimal.ZERO;

        if (totalNetCDF.compareTo(BigDecimal.ZERO) > 0) {
            pct = margeCDF.multiply(BigDecimal.valueOf(100))
                    .divide(totalNetCDF, 2, RoundingMode.HALF_UP);
        }

        kpis.add(RapportVenteKpiResponse.builder()
                .cst(cst)

                .totalNet(totalNetCDF)
                .totalPmp(totalPmpCDF)
                .marge(margeCDF)

                .totalNetCDF(totalNetCDF)
                .totalNetUSD(totalNetUSD)

                .totalPmpCDF(totalPmpCDF)
                .totalPmpUSD(totalPmpUSD)

                .margeCDF(margeCDF)
                .margeUSD(margeUSD)

                .pourcentageMarge(pct)
                .build());
    }

    RapportVenteKpiResponse totalGeneral = buildTotalGeneral(kpis);

    return RapportVentePosResponse.builder()
            .kpis(kpis)
            .details(details)
            .totalGeneral(totalGeneral)
            .build();
}

    private BigDecimal toUSD(BigDecimal montantCDF, BigDecimal tauxChange) {
        if (montantCDF == null) {
            return BigDecimal.ZERO;
        }

        if (tauxChange == null || tauxChange.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return montantCDF.divide(tauxChange, 2, RoundingMode.HALF_UP);
    }

    private RapportVenteKpiResponse buildTotalGeneral(List<RapportVenteKpiResponse> kpis) {

        BigDecimal totalNetCDF = kpis.stream()
                .map(x -> nvl(x.getTotalNetCDF()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPmpCDF = kpis.stream()
                .map(x -> nvl(x.getTotalPmpCDF()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal margeCDF = kpis.stream()
                .map(x -> nvl(x.getMargeCDF()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNetUSD = kpis.stream()
                .map(x -> nvl(x.getTotalNetUSD()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPmpUSD = kpis.stream()
                .map(x -> nvl(x.getTotalPmpUSD()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal margeUSD = kpis.stream()
                .map(x -> nvl(x.getMargeUSD()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pct = BigDecimal.ZERO;

        if (totalNetCDF.compareTo(BigDecimal.ZERO) > 0) {
            pct = margeCDF.multiply(BigDecimal.valueOf(100))
                    .divide(totalNetCDF, 2, RoundingMode.HALF_UP);
        }

        return RapportVenteKpiResponse.builder()
                .cst("Total général")

                .totalNet(totalNetCDF)
                .totalPmp(totalPmpCDF)
                .marge(margeCDF)

                .totalNetCDF(totalNetCDF)
                .totalPmpCDF(totalPmpCDF)
                .margeCDF(margeCDF)

                .totalNetUSD(totalNetUSD)
                .totalPmpUSD(totalPmpUSD)
                .margeUSD(margeUSD)

                .pourcentageMarge(pct)
                .build();
    }

    private BigDecimal safeTaux(BigDecimal taux) {
        if (taux == null || taux.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return taux;
    }

}