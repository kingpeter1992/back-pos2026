package com.king.pos.ImplementServices;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.king.pos.Dao.ProduitRepository;
import com.king.pos.Dao.VenteRepository;
import com.king.pos.Dto.LigneVenteRequest;
import com.king.pos.Dto.VenteRequest;
import com.king.pos.Dto.Response.ProduitPosResponse;
import com.king.pos.Dto.Response.VenteLigneResponse;
import com.king.pos.Dto.Response.VenteResponse;
import com.king.pos.Entitys.LigneVente;
import com.king.pos.Entitys.Produit;
import com.king.pos.Entitys.Vente;
import com.king.pos.Handllers.BusinessException;
import com.king.pos.Handllers.ResourceNotFoundException;
import com.king.pos.Interface.StockService;
import com.king.pos.Interface.VenteService;
import com.king.pos.enums.StatutVente;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class VenteServiceImpl implements VenteService {


    private final VenteRepository venteRepository;
    private final ProduitRepository produitRepository;
    private final StockService stockService;

   @Override
    public VenteResponse enregistrerVente(VenteRequest request) {
        validateRequest(request);

        Vente vente = Vente.builder()
                .ticketNumero(trimToNull(request.getTicketNumero()))
                .clientNom(hasText(request.getClientNom()) ? request.getClientNom().trim() : "CLIENT DIVERS")
                .caissier(trimToNull(request.getCaissier()))
                .modePaiement(request.getModePaiement())
                .montantRecu(nvl(request.getMontantRecu()))
                .monnaie(nvl(request.getMonnaie()))
                .totalHT(nvl(request.getSousTotal()))
                .totalRemise(nvl(request.getTotalRemise()))
                .totalTTC(BigDecimal.ZERO)
               .statut(StatutVente.VALIDE)
                .build();

        List<LigneVente> lignes = new ArrayList<>();
        BigDecimal sousTotalGlobal = BigDecimal.ZERO;
        BigDecimal remiseGlobale = BigDecimal.ZERO;
        BigDecimal totalGeneral = BigDecimal.ZERO;

        for (LigneVenteRequest ligneRequest : request.getLignes()) {
            Produit produit = produitRepository.findById(ligneRequest.getProduitId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Produit introuvable : " + ligneRequest.getProduitId()
                    ));

            if (!Boolean.TRUE.equals(produit.getActif())) {
                throw new BusinessException("Produit inactif : " + produit.getNom());
            }

            if (ligneRequest.getQuantite() == null || ligneRequest.getQuantite() <= 0) {
                throw new BusinessException("La quantité doit être supérieure à 0 pour le produit : " + produit.getNom());
            }

            BigDecimal quantite = BigDecimal.valueOf(ligneRequest.getQuantite());
            BigDecimal prix = nvl(ligneRequest.getPrix());
            BigDecimal remise = nvl(ligneRequest.getRemise());

            if (prix.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Le prix doit être supérieur à zéro pour le produit : " + produit.getNom());
            }

            BigDecimal montantBrutLigne = prix.multiply(quantite);
            BigDecimal totalLigne = montantBrutLigne.subtract(remise);

            if (totalLigne.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("Le total de ligne ne peut pas être négatif pour le produit : " + produit.getNom());
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
            stockService.retirerStock(
                    ligne.getProduit().getId(),
                    ligne.getQuantite(),
                    "VENTE_" + saved.getId(),
                    "Sortie stock après vente POS - Ticket " + saved.getTicketNumero()
            );
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public List<VenteResponse> getAllVente() {
        return venteRepository.findAll(Sort.by(Sort.Direction.DESC, "dateVente"))
                .stream()
                .map(this::mapToResponse)
                .toList();
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
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
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


    @Override
@Transactional
public VenteResponse annulerVente(Long venteId) {
    Vente vente = venteRepository.findById(venteId)
            .orElseThrow(() -> new ResourceNotFoundException("Vente introuvable : " + venteId));

    if (vente.getStatut() == StatutVente.ANNULEE) {
        throw new BusinessException("Cette vente est déjà annulée.");
    }

    if (vente.getLignes() == null || vente.getLignes().isEmpty()) {
        throw new BusinessException("Impossible d'annuler une vente sans lignes.");
    }

    for (LigneVente ligne : vente.getLignes()) {
        stockService.ajouterStock(
                ligne.getProduit().getId(),
                ligne.getQuantite(),
                "ANNULATION_VENTE_" + vente.getId(),
                "Retour stock après annulation vente - Ticket " + vente.getTicketNumero()
        );
    }

    vente.setStatut(StatutVente.ANNULEE);

    Vente saved = venteRepository.save(vente);
    return mapToResponse(saved);
}
}