package com.king.pos.ImplementServices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.king.pos.Dao.*;
import com.king.pos.Dto.ReceptionLocatorLigneRequest;
import com.king.pos.Dto.ReceptionLocatorRequest;
import com.king.pos.Dto.Response.ReceptionLocatorPreparationLigneResponse;
import com.king.pos.Dto.Response.ReceptionLocatorPreparationResponse;
import com.king.pos.Entitys.Depot;
import com.king.pos.Entitys.Locator;
import com.king.pos.Entitys.Produit;
import com.king.pos.Entitys.ProduitLocator;
import com.king.pos.Entitys.ReceptionAchat;
import com.king.pos.Entitys.ReceptionLocatorLigne;
import com.king.pos.Entitys.StockLot;
import com.king.pos.Handllers.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReceptionLocatorServiceImpl {

    private final ReceptionAchatRepository receptionAchatRepository;
    private final LocatorRepository locatorRepository;
    private final ProduitRepository produitRepository;
    private final ProduitLocatorRepository produitLocatorRepository;
    private final ReceptionLocatorLigneRepository receptionLocatorLigneRepository;
            private final StockLotRepository stockLotRepository;

    @Transactional(readOnly = true)
    public ReceptionLocatorPreparationResponse getPreparationByReceptionId(Long receptionId) {
        ReceptionAchat reception = receptionAchatRepository.findById(receptionId)
                .orElseThrow(() -> new BusinessException("Réception introuvable"));

        Long depotId = reception.getDepot().getId();

        List<ReceptionLocatorPreparationLigneResponse> lignes = reception.getLignes()
                .stream()
                .map(ligne -> {
                    ProduitLocator produitLocator = produitLocatorRepository
                            .findByProduitIdAndDepotId(ligne.getProduit().getId(), depotId)
                            .orElse(null);

                    return ReceptionLocatorPreparationLigneResponse.builder()
                            .produitId(ligne.getProduit().getId())
                            .produitNom(ligne.getProduit().getNom())
                            .quantiteRecue(ligne.getQuantiteRecue())
                            .locatorId(produitLocator != null ? produitLocator.getLocator().getId() : null)
                            .locatorCode(produitLocator != null ? produitLocator.getLocator().getCode() : null)
                            .build();
                })
                .toList();

        return ReceptionLocatorPreparationResponse.builder()
                .receptionId(reception.getId())
                .refReception(reception.getRefReception())
                .depotId(reception.getDepot().getId())
                .depotNom(reception.getDepot().getNom())
                .lignes(lignes)
                .build();
    }

    public void affecterLocators(Long receptionId, ReceptionLocatorRequest request) {
        ReceptionAchat reception = receptionAchatRepository.findById(receptionId)
                .orElseThrow(() -> new BusinessException("Réception introuvable"));

        if (request == null || request.getLignes() == null || request.getLignes().isEmpty()) {
            throw new BusinessException("Aucune ligne locator à enregistrer.");
        }

        Depot depot = reception.getDepot();


        for (ReceptionLocatorLigneRequest ligneRequest : request.getLignes()) {
            if (ligneRequest.getProduitId() == null) {
                throw new BusinessException("Produit manquant sur une ligne locator.");
            }
            

            Produit produit = produitRepository.findById(ligneRequest.getProduitId())
                    .orElseThrow(() -> new BusinessException("Produit introuvable"));

            String code = ligneRequest.getLocatorCode() != null
                    ? ligneRequest.getLocatorCode().trim().toUpperCase()
                    : "";

            if (code.isBlank()) {
                throw new BusinessException("Le locator est obligatoire pour le produit : " + produit.getNom());
            }

            BigDecimal quantiteRangee = ligneRequest.getQuantiteRangee() != null
                    ? ligneRequest.getQuantiteRangee()
                    : BigDecimal.ZERO;

            if (quantiteRangee.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("La quantité rangée doit être > 0 pour le produit : " + produit.getNom());
            }

            Locator locator = locatorRepository.findByDepotIdAndCodeIgnoreCase(depot.getId(), code)
                    .orElseGet(() -> locatorRepository.save(
                            Locator.builder()
                                    .depot(depot)
                                    .code(code)
                                    .libelle(code)
                                    .actif(true)
                                    .build()
                    ));

            receptionLocatorLigneRepository.save(
                    ReceptionLocatorLigne.builder()
                            .reception(reception)
                            .produit(produit)
                            .locator(locator)
                            .quantiteRangee(quantiteRangee)
                            .dateCreation(LocalDate.now())
                            .build()
            );

            ProduitLocator produitLocator = produitLocatorRepository
                    .findByProduitIdAndDepotId(produit.getId(), depot.getId())
                    .orElse(
                            ProduitLocator.builder()
                                    .produit(produit)
                                    .depot(depot)
                                    .locator(locator)
                                    .dateAffectation(LocalDate.now())
                                    .actif(true)
                                    .build()
                    );

            produitLocator.setLocator(locator);
            produitLocator.setDateAffectation(LocalDate.now());
            produitLocator.setActif(true);

            produitLocatorRepository.save(produitLocator);
        }
    }
}
