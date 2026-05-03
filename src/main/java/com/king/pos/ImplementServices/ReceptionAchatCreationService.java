package com.king.pos.ImplementServices;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.king.pos.Dao.CommandeAchatRepository;
import com.king.pos.Dao.DepotRepository;
import com.king.pos.Dao.FournisseurRepository;
import com.king.pos.Dao.LocatorRepository;
import com.king.pos.Dao.ProduitRepository;
import com.king.pos.Dao.ReceptionAchatRepository;
import com.king.pos.Dto.CreateReceptionAchatRequest;
import com.king.pos.Dto.ReceptionAchatLigneRequest;
import com.king.pos.Dto.TransactionStockRequest;
import com.king.pos.Dto.Response.ReceptionAchatLigneResponse;
import com.king.pos.Dto.Response.ReceptionAchatResponse;
import com.king.pos.Entitys.CommandeAchat;
import com.king.pos.Entitys.CommandeAchatLigne;
import com.king.pos.Entitys.Depot;
import com.king.pos.Entitys.Fournisseur;
import com.king.pos.Entitys.Produit;
import com.king.pos.Entitys.ReceptionAchat;
import com.king.pos.Entitys.ReceptionAchatLigne;
import com.king.pos.Entitys.StockLot;
import com.king.pos.enums.Devise;
import com.king.pos.enums.StatutCommandeFournisseur;
import com.king.pos.enums.StatutReceptionAchat;
import com.king.pos.enums.TypeMouvementStock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class ReceptionAchatCreationService {

    private static final Logger log = LoggerFactory.getLogger(ReceptionAchatCreationService.class);

    private final ReceptionAchatRepository receptionAchatRepository;
    private final CommandeAchatRepository commandeAchatRepository;
    private final ProduitRepository produitRepository;
    private final FournisseurRepository fournisseurRepository;
    private final DepotRepository depotRepository;
    private final TransactionStockService transactionStockService;
    private final StockLotService stockLotService;
    private final LocatorRepository locatorRepository;

    @Transactional
    public ReceptionAchatResponse create(CreateReceptionAchatRequest request) {
        if (request.getDepotId() == null) {
            throw new IllegalStateException("Le dépôt est obligatoire.");
        }

        if (request.getFournisseurId() == null) {
            throw new IllegalStateException("Le fournisseur est obligatoire.");
        }

        if (request.getLignes() == null || request.getLignes().isEmpty()) {
            throw new IllegalStateException("La réception doit contenir au moins une ligne.");
        }

        Set<Long> produitIds = new HashSet<>();
        for (ReceptionAchatLigneRequest l : request.getLignes()) {
            if (l.getProduitId() == null) {
                throw new IllegalStateException("Le produit est obligatoire sur chaque ligne.");
            }
            if (!produitIds.add(l.getProduitId())) {
                throw new IllegalStateException(
                        "Un produit ne peut pas être présent plusieurs fois dans une même réception.");
            }
        }

        Depot depot = depotRepository.findById(request.getDepotId())
                .orElseThrow(() -> new EntityNotFoundException("Dépôt introuvable"));

        Fournisseur fournisseur = fournisseurRepository.findById(request.getFournisseurId())
                .orElseThrow(() -> new EntityNotFoundException("Fournisseur introuvable"));

        CommandeAchat commande = null;
        Map<Long, CommandeAchatLigne> lignesCommandeMap = Collections.emptyMap();

        if (request.getCommandeAchatId() != null) {
            commande = commandeAchatRepository.findById(request.getCommandeAchatId())
                    .orElseThrow(() -> new EntityNotFoundException("Commande introuvable"));

            if (!commande.getFournisseur().getId().equals(fournisseur.getId())) {
                throw new IllegalStateException(
                        "Le fournisseur de la réception ne correspond pas au fournisseur de la commande.");
            }

            if (commande.getStatut() == StatutCommandeFournisseur.RECEPTIONNEE) {
                throw new IllegalStateException("Cette commande est déjà totalement réceptionnée.");
            }

            lignesCommandeMap = commande.getLignes().stream()
                    .collect(Collectors.toMap(l -> l.getProduit().getId(), Function.identity()));
        }

        ReceptionAchat reception = new ReceptionAchat();
        reception.setCommandeAchat(commande);
        reception.setRefReception("RECA-" + System.currentTimeMillis());
        reception.setDateReception(
                request.getDateReception() != null ? request.getDateReception() : LocalDate.now());
        reception.setDepot(depot);
        reception.setFournisseur(fournisseur);
        reception.setStatut(StatutReceptionAchat.TERMINEE);
        BigDecimal tauxReception = nvl(request.getTauxChangeUtilise());

        if (tauxReception.compareTo(BigDecimal.ZERO) <= 0) {
            tauxReception = nvl(request.getTaux());
        }

        if (tauxReception.compareTo(BigDecimal.ZERO) <= 0 && commande != null) {
            tauxReception = nvl(commande.getTauxChangeUtilise());
        }

        if (tauxReception.compareTo(BigDecimal.ZERO) <= 0 && commande != null) {
            tauxReception = nvl(commande.getTaux());
        }

        if (tauxReception.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Le taux de change de la réception est obligatoire.");
        }

        reception.setDevise(Devise.CDF);
        reception.setTaux(scale6(tauxReception));
        reception.setTauxChangeUtilise(scale6(tauxReception));
        reception.setObservateur(request.getObservateur());
        reception.setFraisTransport(nvl(request.getFraisTransport()));
        reception.setFraisDouane(nvl(request.getFraisDouane()));
        reception.setFraisManutention(nvl(request.getFraisManutention()));
        reception.setAutresFrais(nvl(request.getAutresFrais()));

        List<ReceptionAchatLigne> lignesReception = new ArrayList<>();
        BigDecimal totalMarchandise = BigDecimal.ZERO;

        for (ReceptionAchatLigneRequest l : request.getLignes()) {
            Produit produit = produitRepository.findById(l.getProduitId())
                    .orElseThrow(() -> new EntityNotFoundException("Produit introuvable"));

            if (l.getQuantiteRecue() == null || l.getQuantiteRecue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException(
                        "La quantité reçue doit être supérieure à 0 pour le produit " + produit.getNom());
            }

            if (l.getPrixAchatUnitaire() == null || l.getPrixAchatUnitaire().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException(
                        "Le prix d'achat unitaire doit être supérieur à 0 pour le produit " + produit.getNom());
            }

            // VALIDATION METIER PEREMPTION
            // Si tu as un champ produit.getPerissable(), garde ce test.
            // Sinon rends temporairement la date obligatoire pour les produits concernés
            // par ton métier.
            if (produit.isPerissable() && l.getDatePeremption() == null) {
                throw new IllegalStateException(
                        "La date de péremption est obligatoire pour le produit " + produit.getNom());
            }

            if (l.getDatePeremption() != null && l.getDatePeremption().isBefore(reception.getDateReception())) {
                throw new IllegalStateException(
                        "La date de péremption ne peut pas être antérieure à la date de réception pour le produit "
                                + produit.getNom());
            }

            BigDecimal qteRecue = scale3(l.getQuantiteRecue());
            BigDecimal tauxLigne = nvl(l.getTauxChangeUtilise());

            if (tauxLigne.compareTo(BigDecimal.ZERO) <= 0) {
                tauxLigne = tauxReception;
            }

            BigDecimal prixAchatFc = nvl(l.getPrixAchatUnitaireFc());

            if (prixAchatFc.compareTo(BigDecimal.ZERO) <= 0) {
                prixAchatFc = nvl(l.getPrixAchatUnitaire());
            }

            BigDecimal prixAchatUsd = nvl(l.getPrixAchatUnitaireUsd());

            if (prixAchatUsd.compareTo(BigDecimal.ZERO) <= 0 && tauxLigne.compareTo(BigDecimal.ZERO) > 0) {
                prixAchatUsd = prixAchatFc.divide(tauxLigne, 2, RoundingMode.HALF_UP);
            }

            BigDecimal prixAchat = scale6(prixAchatFc);
            if (commande != null) {
                CommandeAchatLigne ligneCommande = lignesCommandeMap.get(produit.getId());

                if (ligneCommande == null) {
                    throw new IllegalStateException(
                            "Le produit " + produit.getNom() + " n'est pas présent dans la commande.");
                }

                BigDecimal qteCommandee = scale3(nvl(ligneCommande.getQuantiteCommandee()));
                BigDecimal qteDejaRecue = scale3(nvl(ligneCommande.getQuantiteRecue()));
                BigDecimal qteRestante = scale3(qteCommandee.subtract(qteDejaRecue));

                if (qteDejaRecue.compareTo(qteCommandee) >= 0) {
                    throw new IllegalStateException(
                            "Le produit " + produit.getNom() + " est déjà totalement réceptionné.");
                }

                if (qteRecue.compareTo(qteRestante) > 0) {
                    throw new IllegalStateException(
                            "La quantité reçue du produit " + produit.getNom()
                                    + " dépasse la quantité restante à recevoir (" + qteRestante + ").");
                }
            }

            BigDecimal montantAchat = scale2(qteRecue.multiply(prixAchat));
            BigDecimal montantLigneFc = nvl(l.getMontantLigneFc());

            if (montantLigneFc.compareTo(BigDecimal.ZERO) <= 0) {
                montantLigneFc = montantAchat;
            }

            BigDecimal montantLigneUsd = nvl(l.getMontantLigneUsd());

            if (montantLigneUsd.compareTo(BigDecimal.ZERO) <= 0 && tauxLigne.compareTo(BigDecimal.ZERO) > 0) {
                montantLigneUsd = montantLigneFc.divide(tauxLigne, 2, RoundingMode.HALF_UP);
            }

            ReceptionAchatLigne ligneReception = new ReceptionAchatLigne();
            ligneReception.setReceptionAchat(reception);
            ligneReception.setProduit(produit);
            ligneReception.setQuantiteRecue(qteRecue);
            ligneReception.setPrixAchatUnitaire(prixAchat);
            ligneReception.setMontantAchat(montantAchat);
            ligneReception.setTauxChangeUtilise(scale6(tauxLigne));

            ligneReception.setPrixAchatUnitaireFc(scale2(prixAchatFc));
            ligneReception.setPrixAchatUnitaireUsd(scale2(prixAchatUsd));

            ligneReception.setMontantLigneFc(scale2(montantLigneFc));
            ligneReception.setMontantLigneUsd(scale2(montantLigneUsd));

            ligneReception.setCommentaire(l.getCommentaire());
            ligneReception.setPartFrais(BigDecimal.ZERO);
            ligneReception.setFraisUnitaire(BigDecimal.ZERO);
            ligneReception.setCoutUnitaireFinal(prixAchat);

            ligneReception.setDatePeremption(l.getDatePeremption());
            ligneReception.setNumeroLot(l.getNumeroLot());
            lignesReception.add(ligneReception);
            totalMarchandise = totalMarchandise.add(montantAchat);
        }

        reception.setLignes(lignesReception);
        reception.setTotalMarchandise(scale2(totalMarchandise));

        BigDecimal totalFrais = totalFrais(reception);
        reception.setTotalFrais(totalFrais);
        reception.setTotalGeneral(scale2(totalMarchandise.add(totalFrais)));

        BigDecimal montantMarchandiseFc = scale2(totalMarchandise);
        BigDecimal montantFraisFc = scale2(totalFrais);
        BigDecimal montantTotalFc = scale2(totalMarchandise.add(totalFrais));

        BigDecimal montantMarchandiseUsd = tauxReception.compareTo(BigDecimal.ZERO) > 0
                ? montantMarchandiseFc.divide(tauxReception, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal montantFraisUsd = tauxReception.compareTo(BigDecimal.ZERO) > 0
                ? montantFraisFc.divide(tauxReception, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal montantTotalUsd = tauxReception.compareTo(BigDecimal.ZERO) > 0
                ? montantTotalFc.divide(tauxReception, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        reception.setMontantMarchandiseFc(montantMarchandiseFc);
        reception.setMontantMarchandiseUsd(montantMarchandiseUsd);

        reception.setMontantFraisFc(montantFraisFc);
        reception.setMontantFraisUsd(montantFraisUsd);

        reception.setMontantTotalFc(montantTotalFc);
        reception.setMontantTotalUsd(montantTotalUsd);

        repartirFraisSurLignes(reception);

        ReceptionAchat savedReception = receptionAchatRepository.save(reception);

        for (ReceptionAchatLigne ligne : savedReception.getLignes()) {
            entreeStock(ligne, savedReception);
        }

        if (commande != null) {
            updateCommandeAfterReception(savedReception);
        }

        return mapToResponse(savedReception);
    }

    private void updateCommandeAfterReception(ReceptionAchat reception) {
        CommandeAchat commande = reception.getCommandeAchat();
        if (commande == null)
            return;

        for (ReceptionAchatLigne ligneReception : reception.getLignes()) {
            CommandeAchatLigne ligneCommande = commande.getLignes().stream()
                    .filter(l -> l.getProduit().getId().equals(ligneReception.getProduit().getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Produit introuvable dans la commande : " + ligneReception.getProduit().getNom()));

            BigDecimal ancienneQteRecue = scale3(nvl(ligneCommande.getQuantiteRecue()));
            BigDecimal nouvelleQteRecue = scale3(ancienneQteRecue.add(nvl(ligneReception.getQuantiteRecue())));
            BigDecimal qteCommandee = scale3(nvl(ligneCommande.getQuantiteCommandee()));

            if (nouvelleQteRecue.compareTo(qteCommandee) > 0) {
                throw new IllegalStateException(
                        "La quantité reçue cumulée du produit " + ligneReception.getProduit().getNom()
                                + " dépasse la quantité commandée.");
            }

            ligneCommande.setQuantiteRecue(nouvelleQteRecue);
        }

        boolean commandeComplete = commande.getLignes().stream()
                .allMatch(l -> scale3(nvl(l.getQuantiteRecue()))
                        .compareTo(scale3(nvl(l.getQuantiteCommandee()))) >= 0);

        if (commandeComplete) {
            commande.setStatut(StatutCommandeFournisseur.RECEPTIONNEE);
        } else {
            commande.setStatut(StatutCommandeFournisseur.RECEPTION_PARTIELLE);
        }

        commandeAchatRepository.save(commande);
    }

    private void entreeStock(ReceptionAchatLigne ligne, ReceptionAchat reception) {
        if (ligne == null) {
            throw new IllegalStateException("La ligne de réception est obligatoire.");
        }

        if (ligne.getProduit() == null) {
            throw new IllegalStateException("Le produit est obligatoire pour l'entrée en stock.");
        }

        if (reception == null || reception.getDepot() == null) {
            throw new IllegalStateException("Le dépôt de réception est obligatoire.");
        }

        transactionStockService.appliquerTransaction(
                TransactionStockRequest.builder()
                        .typeTransaction(TypeMouvementStock.ENTREE_ACHAT)
                        .produit(ligne.getProduit())
                        .depot(reception.getDepot())
                        .quantite(ligne.getQuantiteRecue())

                        // FC utilisé pour PMP / valeur stock
                        .prixUnitaire(nvl(ligne.getPrixAchatUnitaireFc()).compareTo(BigDecimal.ZERO) > 0
                                ? ligne.getPrixAchatUnitaireFc()
                                : ligne.getPrixAchatUnitaire())

                        .fraisUnitaire(ligne.getFraisUnitaire())
                        .coutUnitaireFinal(ligne.getCoutUnitaireFinal())

                        .tauxChangeUtilise(reception.getTauxChangeUtilise())

                        .prixUnitaireFc(ligne.getPrixAchatUnitaireFc())
                        .prixUnitaireUsd(ligne.getPrixAchatUnitaireUsd())

                        .fraisUnitaireFc(ligne.getFraisUnitaire())
                        .fraisUnitaireUsd(ligne.getFraisUnitaireUsd())

                        .coutUnitaireFinalFc(ligne.getCoutUnitaireFinal())
                        .coutUnitaireFinalUsd(ligne.getCoutUnitaireFinalUsd())

                        .montantLigneFc(ligne.getMontantFinalLigneFc())
                        .montantLigneUsd(ligne.getMontantFinalLigneUsd())

                        .tauxChangeUtilise(reception.getTauxChangeUtilise())

                        .prixUnitaireFc(ligne.getPrixAchatUnitaireFc())
                        .prixUnitaireUsd(ligne.getPrixAchatUnitaireUsd())

                        .fraisUnitaireFc(ligne.getFraisUnitaire())
                        .fraisUnitaireUsd(ligne.getFraisUnitaireUsd())

                        .coutUnitaireFinalFc(ligne.getCoutUnitaireFinal())
                        .coutUnitaireFinalUsd(ligne.getCoutUnitaireFinalUsd())

                        .montantLigneFc(ligne.getMontantFinalLigneFc())
                        .montantLigneUsd(ligne.getMontantFinalLigneUsd())

                        .referenceDocument(reception.getRefReception())
                        .sourceDocument("RECEPTION_ACHAT")
                        .sourceDocumentId(reception.getId())
                        .libelle("Entrée stock réception " + reception.getRefReception()
                                + " - Produit: " + ligne.getProduit().getNom()
                                + " - Taux: " + reception.getTauxChangeUtilise())
                        .utilisateur("SYSTEM")
                        .build());

        StockLot stockLot = new StockLot();

        stockLot.setCoutUnitaireFinal(ligne.getCoutUnitaireFinal());
        stockLot.setFraisUnitaire(ligne.getFraisUnitaire());
        stockLot.setPrixUnitaire(
                nvl(ligne.getPrixAchatUnitaireFc()).compareTo(BigDecimal.ZERO) > 0
                        ? ligne.getPrixAchatUnitaireFc()
                        : ligne.getPrixAchatUnitaire());

        stockLot.setTauxChangeUtilise(reception.getTauxChangeUtilise());

        stockLot.setPrixUnitaireFc(
                nvl(ligne.getPrixAchatUnitaireFc()).compareTo(BigDecimal.ZERO) > 0
                        ? ligne.getPrixAchatUnitaireFc()
                        : ligne.getPrixAchatUnitaire());
        stockLot.setPrixUnitaireUsd(ligne.getPrixAchatUnitaireUsd());

        stockLot.setFraisUnitaireFc(ligne.getFraisUnitaire());
        stockLot.setFraisUnitaireUsd(ligne.getFraisUnitaireUsd());

        stockLot.setCoutUnitaireFinalFc(ligne.getCoutUnitaireFinal());
        stockLot.setCoutUnitaireFinalUsd(ligne.getCoutUnitaireFinalUsd());

        stockLot.setMontantLigneFc(ligne.getMontantFinalLigneFc());
        stockLot.setMontantLigneUsd(ligne.getMontantFinalLigneUsd());

        stockLot.setQuantiteDisponible(ligne.getQuantiteRecue());
        stockLot.setQuantiteInitiale(ligne.getQuantiteRecue());

        stockLot.setDatePeremption(ligne.getDatePeremption());
        stockLot.setNumeroLot(ligne.getNumeroLot());

        stockLot.setProduit(ligne.getProduit());
        stockLot.setDepot(reception.getDepot());

        stockLot.setDateCreation(reception.getDateReception());
        stockLot.setDateModification(reception.getDateReception());
        stockLot.setDateEntree(reception.getDateReception());

        stockLot.setReferenceDocument(reception.getRefReception());
        stockLot.setSourceDocument("RECEPTION_ACHAT");
        stockLot.setSourceDocumentId(reception.getId());

        stockLotService.creerLotEntree(stockLot);
    }

    private void repartirFraisSurLignes(ReceptionAchat reception) {
        BigDecimal totalMarchandiseFc = scale2(nvl(reception.getTotalMarchandise()));
        BigDecimal totalFraisFc = scale2(nvl(reception.getTotalFrais()));
        BigDecimal taux = scale6(nvl(reception.getTauxChangeUtilise()));

        if (totalFraisFc.compareTo(BigDecimal.ZERO) <= 0 || totalMarchandiseFc.compareTo(BigDecimal.ZERO) <= 0) {
            for (ReceptionAchatLigne ligne : reception.getLignes()) {
                BigDecimal prixFc = scale6(nvl(ligne.getPrixAchatUnitaire()));
                BigDecimal prixUsd = taux.compareTo(BigDecimal.ZERO) > 0
                        ? div(prixFc, taux, 6)
                        : BigDecimal.ZERO;

                BigDecimal qte = scale3(nvl(ligne.getQuantiteRecue()));
                BigDecimal montantFinalFc = scale2(qte.multiply(prixFc));
                BigDecimal montantFinalUsd = taux.compareTo(BigDecimal.ZERO) > 0
                        ? div(montantFinalFc, taux, 2)
                        : BigDecimal.ZERO;

                ligne.setPartFrais(BigDecimal.ZERO);
                ligne.setPartFraisUsd(BigDecimal.ZERO);

                ligne.setFraisUnitaire(BigDecimal.ZERO);
                ligne.setFraisUnitaireUsd(BigDecimal.ZERO);

                ligne.setCoutUnitaireFinal(prixFc);
                ligne.setCoutUnitaireFinalUsd(prixUsd);

                ligne.setMontantFinalLigne(montantFinalFc);
                ligne.setMontantFinalLigneFc(montantFinalFc);
                ligne.setMontantFinalLigneUsd(montantFinalUsd);
            }
            return;
        }

        BigDecimal totalPartFraisAttribueFc = BigDecimal.ZERO;

        for (int i = 0; i < reception.getLignes().size(); i++) {
            ReceptionAchatLigne ligne = reception.getLignes().get(i);

            BigDecimal partFraisFc;

            if (i == reception.getLignes().size() - 1) {
                partFraisFc = totalFraisFc.subtract(totalPartFraisAttribueFc);
            } else {
                partFraisFc = nvl(ligne.getMontantAchat())
                        .multiply(totalFraisFc)
                        .divide(totalMarchandiseFc, 6, RoundingMode.HALF_UP);

                partFraisFc = scale2(partFraisFc);
                totalPartFraisAttribueFc = totalPartFraisAttribueFc.add(partFraisFc);
            }

            BigDecimal qte = scale3(nvl(ligne.getQuantiteRecue()));

            BigDecimal fraisUnitaireFc = qte.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : div(partFraisFc, qte, 6);

            BigDecimal prixFc = scale6(nvl(ligne.getPrixAchatUnitaire()));
            BigDecimal coutFinalFc = scale6(prixFc.add(fraisUnitaireFc));

            BigDecimal montantFinalFc = scale2(qte.multiply(coutFinalFc));

            BigDecimal partFraisUsd = taux.compareTo(BigDecimal.ZERO) > 0
                    ? div(partFraisFc, taux, 2)
                    : BigDecimal.ZERO;

            BigDecimal fraisUnitaireUsd = taux.compareTo(BigDecimal.ZERO) > 0
                    ? div(fraisUnitaireFc, taux, 6)
                    : BigDecimal.ZERO;

            BigDecimal coutFinalUsd = taux.compareTo(BigDecimal.ZERO) > 0
                    ? div(coutFinalFc, taux, 6)
                    : BigDecimal.ZERO;

            BigDecimal montantFinalUsd = taux.compareTo(BigDecimal.ZERO) > 0
                    ? div(montantFinalFc, taux, 2)
                    : BigDecimal.ZERO;

            ligne.setPartFrais(scale2(partFraisFc));
            ligne.setPartFraisUsd(scale2(partFraisUsd));

            ligne.setFraisUnitaire(scale6(fraisUnitaireFc));
            ligne.setFraisUnitaireUsd(scale6(fraisUnitaireUsd));

            ligne.setCoutUnitaireFinal(scale6(coutFinalFc));
            ligne.setCoutUnitaireFinalUsd(scale6(coutFinalUsd));

            ligne.setMontantFinalLigne(scale2(montantFinalFc));
            ligne.setMontantFinalLigneFc(scale2(montantFinalFc));
            ligne.setMontantFinalLigneUsd(scale2(montantFinalUsd));
        }
    }

    private BigDecimal div(BigDecimal a, BigDecimal b, int scale) {
        if (b == null || b.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return nvl(a).divide(b, scale, RoundingMode.HALF_UP);
    }

    public List<ReceptionAchatResponse> getAll() {
        return receptionAchatRepository.findAllWithReferences()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ReceptionAchatResponse getById(Long id) {
        ReceptionAchat reception = receptionAchatRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Réception introuvable : " + id));

        return mapToResponse(reception);
    }

    public List<ReceptionAchatResponse> findByCommande(Long commandeId) {
        return receptionAchatRepository.findByCommandeAchatIdOrderByDateReceptionDesc(commandeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ReceptionAchatResponse mapToResponse(ReceptionAchat reception) {
        ReceptionAchatResponse dto = new ReceptionAchatResponse();

        dto.setId(reception.getId());
        dto.setRefReception(valueOrDefault(reception.getRefReception(), "REC-" + reception.getId()));
        dto.setDateReception(reception.getDateReception());
        dto.setStatut(reception.getStatut() != null ? reception.getStatut().name() : "BROUILLON");

        if (reception.getDepot() != null) {
            dto.setDepotId(reception.getDepot().getId());
            dto.setDepotNom(reception.getDepot().getNom());
        }

        if (reception.getFournisseur() != null) {
            dto.setFournisseurId(reception.getFournisseur().getId());
            dto.setFournisseurNom(reception.getFournisseur().getNom());
        }

        if (reception.getCommandeAchat() != null) {
            dto.setCommandeAchatId(reception.getCommandeAchat().getId());
            dto.setRefCommande(
                    valueOrDefault(
                            reception.getCommandeAchat().getRefCommande(),
                            "CMD-" + reception.getCommandeAchat().getId()));
        }

        List<ReceptionAchatLigneResponse> lignes = reception.getLignes() == null
                ? List.of()
                : reception.getLignes().stream()
                        .map(this::mapLigneToResponse)
                        .toList();

        dto.setLignes(lignes);

        BigDecimal totalMarchandise = safe(reception.getTotalMarchandise());
        BigDecimal totalFrais = safe(reception.getTotalFrais());
        BigDecimal totalGeneral = safe(reception.getTotalGeneral());

        if (totalMarchandise.compareTo(BigDecimal.ZERO) == 0 && !lignes.isEmpty()) {
            totalMarchandise = lignes.stream()
                    .map(l -> safe(l.getMontantAchat()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        if (totalFrais.compareTo(BigDecimal.ZERO) == 0 && !lignes.isEmpty()) {
            totalFrais = lignes.stream()
                    .map(l -> safe(l.getPartFrais()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        if (totalGeneral.compareTo(BigDecimal.ZERO) == 0) {
            totalGeneral = totalMarchandise.add(totalFrais);
        }

        dto.setTauxChangeUtilise(reception.getTauxChangeUtilise());
        dto.setObservateur(reception.getObservateur());

        dto.setMontantMarchandiseFc(safe(reception.getMontantMarchandiseFc()));
        dto.setMontantMarchandiseUsd(safe(reception.getMontantMarchandiseUsd()));

        dto.setMontantFraisFc(safe(reception.getMontantFraisFc()));
        dto.setMontantFraisUsd(safe(reception.getMontantFraisUsd()));

        dto.setMontantTotalFc(safe(reception.getMontantTotalFc()));
        dto.setMontantTotalUsd(safe(reception.getMontantTotalUsd()));
        dto.setTotalMarchandise(totalMarchandise);
        dto.setTotalFrais(totalFrais);
        dto.setTotalGeneral(totalGeneral);

        return dto;
    }

    private ReceptionAchatLigneResponse mapLigneToResponse(ReceptionAchatLigne ligne) {
        ReceptionAchatLigneResponse dto = new ReceptionAchatLigneResponse();

        dto.setId(ligne.getId());

        if (ligne.getProduit() != null) {
            dto.setProduitId(ligne.getProduit().getId());
            dto.setProduitNom(ligne.getProduit().getNom());

            if (ligne.getProduit().getCategorie() != null) {
                dto.setCategorieId(ligne.getProduit().getCategorie().getId());
                dto.setCategorieNom(ligne.getProduit().getCategorie().getNom());
            }
        }

        dto.setQuantiteRecue(safe(ligne.getQuantiteRecue()));
        dto.setPrixAchatUnitaire(safe(ligne.getPrixAchatUnitaire()));

        BigDecimal montantAchat = safe(ligne.getMontantAchat());
        if (montantAchat.compareTo(BigDecimal.ZERO) == 0) {
            montantAchat = safe(ligne.getQuantiteRecue()).multiply(safe(ligne.getPrixAchatUnitaire()));
        }
        dto.setMontantAchat(montantAchat);

        dto.setPartFrais(safe(ligne.getPartFrais()));
        dto.setFraisUnitaire(safe(ligne.getFraisUnitaire()));

        BigDecimal coutUnitaireFinal = safe(ligne.getCoutUnitaireFinal());
        if (coutUnitaireFinal.compareTo(BigDecimal.ZERO) == 0) {
            coutUnitaireFinal = safe(ligne.getPrixAchatUnitaire()).add(safe(ligne.getFraisUnitaire()));
        }
        dto.setCoutUnitaireFinal(coutUnitaireFinal);

        dto.setTauxChangeUtilise(ligne.getTauxChangeUtilise());

        dto.setPrixAchatUnitaireFc(safe(ligne.getPrixAchatUnitaireFc()));
        dto.setPrixAchatUnitaireUsd(safe(ligne.getPrixAchatUnitaireUsd()));

        dto.setMontantLigneFc(safe(ligne.getMontantLigneFc()));
        dto.setMontantLigneUsd(safe(ligne.getMontantLigneUsd()));

        dto.setPartFraisUsd(safe(ligne.getPartFraisUsd()));
        dto.setFraisUnitaireUsd(safe(ligne.getFraisUnitaireUsd()));
        dto.setCoutUnitaireFinalUsd(safe(ligne.getCoutUnitaireFinalUsd()));
        dto.setMontantFinalLigneFc(safe(ligne.getMontantFinalLigneFc()));
        dto.setMontantFinalLigneUsd(safe(ligne.getMontantFinalLigneUsd()));

        dto.setCommentaire(ligne.getCommentaire());
        dto.setDatePeremption(ligne.getDatePeremption());
        dto.setNumeroLot(ligne.getNumeroLot());

        return dto;
    }

    private BigDecimal totalFrais(ReceptionAchat r) {
        return scale2(
                nvl(r.getFraisTransport())
                        .add(nvl(r.getFraisDouane()))
                        .add(nvl(r.getFraisManutention()))
                        .add(nvl(r.getAutresFrais())));
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal scale2(BigDecimal value) {
        return nvl(value).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scale3(BigDecimal value) {
        return nvl(value).setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal scale6(BigDecimal value) {
        return nvl(value).setScale(6, RoundingMode.HALF_UP);
    }

    private String valueOrDefault(String value, String defaultValue) {
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

}