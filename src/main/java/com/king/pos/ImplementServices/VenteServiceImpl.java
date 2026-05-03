package com.king.pos.ImplementServices;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.mapping.Map;
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
import com.king.pos.Entitys.Depot;
import com.king.pos.enums.TypeTransactionStock;

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

@Override
public VenteResponse enregistrerVente(VenteRequest request) {
    validateRequest(request);

    Depot depot = depotRepository.findById(request.getDepotId())
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Dépôt introuvable : " + request.getDepotId()));

    Vente vente = Vente.builder()
            .ticketNumero(trimToNull(request.getTicketNumero()))
            .clientNom(hasText(request.getClientNom())
                    ? request.getClientNom().trim()
                    : "CLIENT DIVERS")
            .caissier(trimToNull(request.getCaissier()))
            .modePaiement(request.getModePaiement())
            .devise(request.getDevise())
            .tauxChange(nvl(request.getTauxChange()))

            .montantRecu(nvl(request.getMontantRecu()))
            .monnaie(nvl(request.getMonnaie()))

            .sousTotalCDF(nvl(request.getSousTotalCDF()))
            .totalRemiseCDF(nvl(request.getTotalRemiseCDF()))
            .totalGeneralCDF(nvl(request.getTotalGeneralCDF()))
            .montantRecuCDF(nvl(request.getMontantRecuCDF()))
            .monnaieCDF(nvl(request.getMonnaieCDF()))

            .sousTotalUSD(nvl(request.getSousTotalUSD()))
            .totalRemiseUSD(nvl(request.getTotalRemiseUSD()))
            .totalGeneralUSD(nvl(request.getTotalGeneralUSD()))
            .montantRecuUSD(nvl(request.getMontantRecuUSD()))
            .monnaieUSD(nvl(request.getMonnaieUSD()))

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

                .prixUSD(nvl(lr.getPrixUSD()))
                .remiseUSD(nvl(lr.getRemiseUSD()))
                .totalUSD(nvl(lr.getTotalUSD()))

                .tauxChange(nvl(lr.getTauxChange()))
                .build();

        lignes.add(ligne);
    }

    vente.setLignes(lignes);

    Vente saved = venteRepository.save(vente);

    appliquerSortieStock(saved);

    return mapToResponse(saved);
}
private void appliquerSortieStock(Vente saved) {
    for (LigneVente ligne : saved.getLignes()) {

        BigDecimal quantiteVendue = nvl(ligne.getQuantite());

        List<LotConsommationResult> consommations =
                sortieStockLotService.consommerEnFefo(
                        ligne.getProduit(),
                        saved.getDepot(),
                        quantiteVendue
                );

        venteLotConsommationService.enregistrerConsommations(
                saved,
                ligne,
                consommations
        );

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
                        .build()
        );
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

    Vente retour = Vente.builder()
            .dateVente(LocalDateTime.now())
            .ticketNumero(genererNumeroRetour(venteOriginale.getTicketNumero()))
            .clientNom(venteOriginale.getClientNom())
            .caissier(venteOriginale.getCaissier())
            .depot(venteOriginale.getDepot())
            .devise(venteOriginale.getDevise())
            .tauxChange(nvl(venteOriginale.getTauxChange()))
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

            .sousTotalUSD(nvl(venteOriginale.getSousTotalUSD()).negate())
            .totalRemiseUSD(nvl(venteOriginale.getTotalRemiseUSD()).negate())
            .totalGeneralUSD(nvl(venteOriginale.getTotalGeneralUSD()).negate())
            .montantRecuUSD(nvl(venteOriginale.getMontantRecuUSD()).negate())
            .monnaieUSD(nvl(venteOriginale.getMonnaieUSD()).negate())
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

                .prixUSD(nvl(ligneOriginale.getPrixUSD()))
                .remiseUSD(nvl(ligneOriginale.getRemiseUSD()).negate())
                .totalUSD(nvl(ligneOriginale.getTotalUSD()).negate())

                .tauxChange(nvl(ligneOriginale.getTauxChange()))

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
                        .quantite(ligne.getQuantite())
                        .prixUnitaire(nvl(ligne.getPrixUnitaire()))
                        .remise(nvl(ligne.getRemise()))
                        .totalLigne(nvl(ligne.getSousTotal()))
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
                .montantRecu(nvl(vente.getMontantRecu()))
                .monnaie(nvl(vente.getMonnaie()))
                .sousTotal(nvl(vente.getTotalHT()))
                .totalRemise(nvl(vente.getTotalRemise()))
                .totalGeneral(nvl(vente.getTotalTTC()))
                .devise(vente.getDevise() != null ? vente.getDevise() : "USD")
                .statut(vente.getStatut() != null ? vente.getStatut().name() : null)
                .lignes(lignes)
                .tauxChange(nvl(vente.getTauxChange()))
                .sousTotalCDF(nvl(vente.getSousTotalCDF()))
                .totalRemiseCDF(nvl(vente.getTotalRemiseCDF()))
                .totalGeneralCDF(nvl(vente.getTotalGeneralCDF()))
                .montantRecuCDF(nvl(vente.getMontantRecuCDF()))
                .monnaieCDF(nvl(vente.getMonnaieCDF()))

                .sousTotalUSD(nvl(vente.getSousTotalUSD()))
                .totalRemiseUSD(nvl(vente.getTotalRemiseUSD()))
                .totalGeneralUSD(nvl(vente.getTotalGeneralUSD()))
                .montantRecuUSD(nvl(vente.getMontantRecuUSD()))
                .monnaieUSD(nvl(vente.getMonnaieUSD()))
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

        for (LigneVente ligne : vente.getLignes()) {

            BigDecimal quantite = nvl(ligne.getQuantite());
            BigDecimal totalNet = nvl(ligne.getSousTotal());

            BigDecimal pmp = nvl(ligne.getPmpAuMomentVente());
            BigDecimal totalPmp = pmp.multiply(quantite);

            BigDecimal marge = totalNet.subtract(totalPmp);

            BigDecimal pourcentageMarge = BigDecimal.ZERO;

            if (totalNet.compareTo(BigDecimal.ZERO) > 0) {
                pourcentageMarge = marge.multiply(BigDecimal.valueOf(100))
                        .divide(totalNet, 2, RoundingMode.HALF_UP);
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

                    .coursDevise(nvl(vente.getTauxChange()))

                    .prixBrut(nvl(ligne.getPrixUnitaire()))
                    .remise(nvl(ligne.getRemise()))
                    .prixNet(nvl(ligne.getPrixUnitaire()).subtract(nvl(ligne.getRemise())))

                    .pmp(pmp)

                    .totalNet(totalNet)
                    .totalPmp(totalPmp)

                    .marge(marge)
                    .pourcentageMarge(pourcentageMarge)

                    .tauxTva(nvl(ligne.getTauxTva()))
                    .totalTtc(totalNet)
                    .build();

            details.add(detail);

            grouped.computeIfAbsent(cst, k -> new ArrayList<>()).add(detail);
        }
    }

    List<RapportVenteKpiResponse> kpis = new ArrayList<>();

    for (var entry : grouped.entrySet()) {

        String cst = entry.getKey();
        List<RapportVenteDetailResponse> lignes = entry.getValue();

        BigDecimal totalNet = lignes.stream()
                .map(RapportVenteDetailResponse::getTotalNet)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPmp = lignes.stream()
                .map(RapportVenteDetailResponse::getTotalPmp)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal marge = totalNet.subtract(totalPmp);

        BigDecimal totalNetCDF = lignes.stream()
                .map(l -> l.getTotalNet().multiply(l.getCoursDevise()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPmpCDF = lignes.stream()
                .map(l -> l.getTotalPmp().multiply(l.getCoursDevise()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal margeCDF = totalNetCDF.subtract(totalPmpCDF);

        BigDecimal pct = BigDecimal.ZERO;

        if (totalNet.compareTo(BigDecimal.ZERO) > 0) {
            pct = marge.multiply(BigDecimal.valueOf(100))
                    .divide(totalNet, 2, RoundingMode.HALF_UP);
        }

        kpis.add(RapportVenteKpiResponse.builder()
                .cst(cst)
                .totalNet(totalNet)
                .totalPmp(totalPmp)
                .marge(marge)
                .totalNetCDF(totalNetCDF)
                .totalPmpCDF(totalPmpCDF)
                .margeCDF(margeCDF)
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



private RapportVenteKpiResponse buildTotalGeneral(List<RapportVenteKpiResponse> kpis) {

    BigDecimal totalNet = kpis.stream()
            .map(RapportVenteKpiResponse::getTotalNet)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalPmp = kpis.stream()
            .map(RapportVenteKpiResponse::getTotalPmp)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal marge = totalNet.subtract(totalPmp);

    BigDecimal totalNetCDF = kpis.stream()
            .map(RapportVenteKpiResponse::getTotalNetCDF)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalPmpCDF = kpis.stream()
            .map(RapportVenteKpiResponse::getTotalPmpCDF)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal margeCDF = totalNetCDF.subtract(totalPmpCDF);

    BigDecimal pct = BigDecimal.ZERO;

    if (totalNet.compareTo(BigDecimal.ZERO) > 0) {
        pct = marge.multiply(BigDecimal.valueOf(100))
                .divide(totalNet, 2, RoundingMode.HALF_UP);
    }

    return RapportVenteKpiResponse.builder()
            .cst("Total général")
            .totalNet(totalNet)
            .totalPmp(totalPmp)
            .marge(marge)
            .totalNetCDF(totalNetCDF)
            .totalPmpCDF(totalPmpCDF)
            .margeCDF(margeCDF)
            .pourcentageMarge(pct)
            .build();
}
}