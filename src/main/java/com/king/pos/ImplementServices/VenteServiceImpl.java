package com.king.pos.ImplementServices;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.king.pos.Dao.ProduitRepository;
import com.king.pos.Dao.VenteLigneRepository;
import com.king.pos.Dao.VenteRepository;
import com.king.pos.Dto.AnnulationVenteRequest;
import com.king.pos.Dto.LigneVenteRequest;
import com.king.pos.Dto.LotConsommationResult;
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
                .orElseThrow(() -> new ResourceNotFoundException("Dépôt introuvable : " + request.getDepotId()));

        Vente vente = Vente.builder()
                .ticketNumero(trimToNull(request.getTicketNumero()))
                .clientNom(hasText(request.getClientNom()) ? request.getClientNom().trim() : "CLIENT DIVERS")
                .caissier(trimToNull(request.getCaissier()))
                .modePaiement(request.getModePaiement())
                .montantRecu(nvl(request.getMontantRecu()))
                .monnaie(nvl(request.getMonnaie()))
                .totalHT(BigDecimal.ZERO)
                .totalRemise(BigDecimal.ZERO)
                .totalTTC(BigDecimal.ZERO)
                .statut(StatutVente.VALIDE)
                .depot(depot)
                .devise(hasText(request.getDevise()) ? request.getDevise().trim() : "CDF")
                .build();

        List<LigneVente> lignes = new ArrayList<>();
        BigDecimal sousTotalGlobal = BigDecimal.ZERO;
        BigDecimal remiseGlobale = BigDecimal.ZERO;
        BigDecimal totalGeneral = BigDecimal.ZERO;

        for (LigneVenteRequest ligneRequest : request.getLignes()) {
            Produit produit = produitRepository.findById(ligneRequest.getProduitId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Produit introuvable : " + ligneRequest.getProduitId()));

            if (!Boolean.TRUE.equals(produit.getActif())) {
                throw new BusinessException("Produit inactif : " + produit.getNom());
            }

            if (ligneRequest.getQuantite() == null || ligneRequest.getQuantite().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(
                        "La quantité doit être supérieure à 0 pour le produit : " + produit.getNom());
            }

            BigDecimal quantite = nvl(ligneRequest.getQuantite());
            BigDecimal prix = nvl(ligneRequest.getPrix());
            BigDecimal remise = nvl(ligneRequest.getRemise());

            if (prix.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Le prix doit être supérieur à zéro pour le produit : " + produit.getNom());
            }

            BigDecimal montantBrutLigne = prix.multiply(quantite);
            BigDecimal totalLigne = montantBrutLigne.subtract(remise);

            if (totalLigne.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(
                        "Le total de ligne ne peut pas être négatif pour le produit : " + produit.getNom());
            }

            LigneVente ligne = LigneVente.builder()
                    .vente(vente)
                    .produit(produit)
                    .quantite(ligneRequest.getQuantite())
                    .prixUnitaire(prix)
                    .remise(remise)
                    .sousTotal(totalLigne)
                    .build();

            lignes.add(ligne);

            sousTotalGlobal = sousTotalGlobal.add(montantBrutLigne);
            remiseGlobale = remiseGlobale.add(remise);
            totalGeneral = totalGeneral.add(totalLigne);
        }

        vente.setLignes(lignes);
        vente.setTotalHT(sousTotalGlobal);
        vente.setTotalRemise(remiseGlobale);
        vente.setTotalTTC(totalGeneral);

        Vente saved = venteRepository.save(vente);

        for (LigneVente ligne : saved.getLignes()) {
            BigDecimal quantiteVendue = (ligne.getQuantite());

            List<LotConsommationResult> consommations = sortieStockLotService.consommerEnFefo(
                    ligne.getProduit(),
                    saved.getDepot(),
                    quantiteVendue);

            venteLotConsommationService.enregistrerConsommations(saved, ligne, consommations);

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

        return mapToResponse(saved);
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

            try {
        // ton code ici
   
        if (venteId == null) {
            throw new BusinessException("Le numéro de vente est obligatoire.");
        }

        Vente venteOriginale = venteRepository.findById(venteId)
                .orElseThrow(() -> new ResourceNotFoundException("Vente introuvable : " + venteId));

        if (request == null || request.getCommentaire() == null || request.getCommentaire().trim().isEmpty()) {
            throw new BusinessException("Le commentaire d'annulation est obligatoire.");
        }

        if (venteOriginale.getLignes() == null || venteOriginale.getLignes().isEmpty()) {
            throw new BusinessException("Impossible d'effectuer un retour sur une vente sans lignes.");
        }

        if (venteOriginale.getDepot() == null) {
            throw new BusinessException("Aucun dépôt n'est associé à cette vente.");
        }

        if (venteRepository.existsByVenteOrigineId(venteOriginale.getId())) {
            throw new BusinessException("Un retour a déjà été généré pour cette vente.");
        }

        List<VenteLotConsommation> consommations = venteLotConsommationService.getByVenteId(venteOriginale.getId());

        if (consommations.isEmpty()) {
            throw new BusinessException(
                    "Retour de vente impossible : aucune traçabilité de lots n'a été trouvée pour cette vente.");
        }

        // 1. Remettre les lots en stock
        sortieStockLotService.remettreEnStockLotsAnnules(consommations);

        // 2. Remettre le stock global
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
                            .build());
        }

        // 3. Créer la nouvelle vente de retour
        Vente retour = new Vente();
        retour.setDateVente(LocalDateTime.now());
        retour.setTicketNumero(genererNumeroRetour(venteOriginale.getTicketNumero()));
        retour.setClientNom(venteOriginale.getClientNom());
        retour.setCaissier(venteOriginale.getCaissier());
        retour.setDepot(venteOriginale.getDepot());
        retour.setDevise(venteOriginale.getDevise());
        retour.setTaux(nvl(venteOriginale.getTaux()));
        retour.setModePaiement(venteOriginale.getModePaiement());
        retour.setStatut(StatutVente.RETOURNEE);
        retour.setVenteOrigine(venteOriginale);
        retour.setCommentaireAnnulation(request.getCommentaire().trim());

        // Montants négatifs
        retour.setTotalHT(nvl(venteOriginale.getTotalHT()).negate());
        retour.setTotalRemise(nvl(venteOriginale.getTotalRemise()).negate());
        retour.setTotalTva(nvl(venteOriginale.getTotalTva()).negate());
        retour.setTotalTTC(nvl(venteOriginale.getTotalTTC()).negate());
        retour.setTotal(nvl(venteOriginale.getTotal()).negate());
        retour.setMontantRecu(nvl(venteOriginale.getMontantRecu()).negate());
        retour.setMonnaie(nvl(venteOriginale.getMonnaie()).negate());

        Vente retourSaved = venteRepository.save(retour);

        // 4. Créer les lignes retour + consommations retour négatives
        List<LigneVente> lignesRetour = new ArrayList<>();

        for (LigneVente ligneOriginale : venteOriginale.getLignes()) {
            LigneVente ligneRetour = new LigneVente();
            ligneRetour.setVente(retourSaved);
            ligneRetour.setProduit(ligneOriginale.getProduit());
            ligneRetour.setTarifVente(ligneOriginale.getTarifVente());

            ligneRetour.setQuantite(nvl(ligneOriginale.getQuantite()).negate());
            ligneRetour.setPrixUnitaire(nvl(ligneOriginale.getPrixUnitaire()));
            ligneRetour.setRemise(nvl(ligneOriginale.getRemise()).negate());
            ligneRetour.setSousTotal(nvl(ligneOriginale.getSousTotal()).negate());

            ligneRetour.setPmpAuMomentVente(nvl(ligneOriginale.getPmpAuMomentVente()));
            ligneRetour.setTauxMarge(nvl(ligneOriginale.getTauxMarge()));
            ligneRetour.setTauxRemiseMax(nvl(ligneOriginale.getTauxRemiseMax()));
            ligneRetour.setTauxRemiseAppliquee(nvl(ligneOriginale.getTauxRemiseAppliquee()));
            ligneRetour.setPrixBrut(nvl(ligneOriginale.getPrixBrut()));
            ligneRetour.setMontantRemise(nvl(ligneOriginale.getMontantRemise()).negate());
            ligneRetour.setPrixUnitaireVente(nvl(ligneOriginale.getPrixUnitaireVente()));
            ligneRetour.setTauxTva(nvl(ligneOriginale.getTauxTva()));

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
                        consommationsLigneOriginale);
            }
        }

        retourSaved.setLignes(lignesRetour);

        return mapToResponse(retourSaved);
         } catch (Exception e) {
        e.printStackTrace();
        throw e;
    }
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
}