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
                throw new IllegalStateException("Un produit ne peut pas être présent plusieurs fois dans une même réception.");
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
                throw new IllegalStateException("Le fournisseur de la réception ne correspond pas au fournisseur de la commande.");
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
                request.getDateReception() != null ? request.getDateReception() : LocalDate.now()
        );
        reception.setDepot(depot);
        reception.setFournisseur(fournisseur);
        reception.setStatut(StatutReceptionAchat.TERMINEE);
        reception.setDevise(request.getDevise() != null ? request.getDevise() : Devise.CDF);
        reception.setTaux(request.getTaux() != null ? request.getTaux() : BigDecimal.ONE);

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
                throw new IllegalStateException("La quantité reçue doit être supérieure à 0 pour le produit " + produit.getNom());
            }

            if (l.getPrixAchatUnitaire() == null || l.getPrixAchatUnitaire().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("Le prix d'achat unitaire doit être supérieur à 0 pour le produit " + produit.getNom());
            }


             // VALIDATION METIER PEREMPTION
            // Si tu as un champ produit.getPerissable(), garde ce test.
            // Sinon rends temporairement la date obligatoire pour les produits concernés par ton métier.
            if (produit.isPerissable() && l.getDatePeremption() == null) {
                throw new IllegalStateException("La date de péremption est obligatoire pour le produit " + produit.getNom());
            }

            if (l.getDatePeremption() != null && l.getDatePeremption().isBefore(reception.getDateReception())) {
                throw new IllegalStateException("La date de péremption ne peut pas être antérieure à la date de réception pour le produit " + produit.getNom());
            }


            BigDecimal qteRecue = scale3(l.getQuantiteRecue());
            BigDecimal prixAchat = scale6(l.getPrixAchatUnitaire());

            if (commande != null) {
                CommandeAchatLigne ligneCommande = lignesCommandeMap.get(produit.getId());

                if (ligneCommande == null) {
                    throw new IllegalStateException(
                            "Le produit " + produit.getNom() + " n'est pas présent dans la commande."
                    );
                }

                BigDecimal qteCommandee = scale3(nvl(ligneCommande.getQuantiteCommandee()));
                BigDecimal qteDejaRecue = scale3(nvl(ligneCommande.getQuantiteRecue()));
                BigDecimal qteRestante = scale3(qteCommandee.subtract(qteDejaRecue));

                if (qteDejaRecue.compareTo(qteCommandee) >= 0) {
                    throw new IllegalStateException(
                            "Le produit " + produit.getNom() + " est déjà totalement réceptionné."
                    );
                }

                if (qteRecue.compareTo(qteRestante) > 0) {
                    throw new IllegalStateException(
                            "La quantité reçue du produit " + produit.getNom()
                                    + " dépasse la quantité restante à recevoir (" + qteRestante + ")."
                    );
                }
            }

            BigDecimal montantAchat = scale2(qteRecue.multiply(prixAchat));

            ReceptionAchatLigne ligneReception = new ReceptionAchatLigne();
            ligneReception.setReceptionAchat(reception);
            ligneReception.setProduit(produit);
            ligneReception.setQuantiteRecue(qteRecue);
            ligneReception.setPrixAchatUnitaire(prixAchat);
            ligneReception.setMontantAchat(montantAchat);
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
        if (commande == null) return;

        for (ReceptionAchatLigne ligneReception : reception.getLignes()) {
            CommandeAchatLigne ligneCommande = commande.getLignes().stream()
                    .filter(l -> l.getProduit().getId().equals(ligneReception.getProduit().getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Produit introuvable dans la commande : " + ligneReception.getProduit().getNom()
                    ));

            BigDecimal ancienneQteRecue = scale3(nvl(ligneCommande.getQuantiteRecue()));
            BigDecimal nouvelleQteRecue = scale3(ancienneQteRecue.add(nvl(ligneReception.getQuantiteRecue())));
            BigDecimal qteCommandee = scale3(nvl(ligneCommande.getQuantiteCommandee()));

            if (nouvelleQteRecue.compareTo(qteCommandee) > 0) {
                throw new IllegalStateException(
                        "La quantité reçue cumulée du produit " + ligneReception.getProduit().getNom()
                                + " dépasse la quantité commandée."
                );
            }

            ligneCommande.setQuantiteRecue(nouvelleQteRecue);
        }

        boolean commandeComplete = commande.getLignes().stream()
                .allMatch(l ->
                        scale3(nvl(l.getQuantiteRecue()))
                                .compareTo(scale3(nvl(l.getQuantiteCommandee()))) >= 0
                );

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
                        .prixUnitaire(ligne.getPrixAchatUnitaire())
                        .fraisUnitaire(ligne.getFraisUnitaire())
                        .coutUnitaireFinal(ligne.getCoutUnitaireFinal())
                        .referenceDocument(reception.getRefReception())
                        .sourceDocument("RECEPTION_ACHAT")
                        .sourceDocumentId(reception.getId())
                        .libelle("Entrée stock réception " + reception.getRefReception()
                                + " - Produit: " + ligne.getProduit().getNom())
                        .utilisateur("SYSTEM")
                        .build()
        );



log.info("Mon message log");
log.error("Erreur ici");
            StockLot stockLot = new StockLot();
                stockLot.setCoutUnitaireFinal(ligne.getCoutUnitaireFinal());
                stockLot.setFraisUnitaire(ligne.getFraisUnitaire());
                stockLot.setPrixUnitaire(ligne.getPrixAchatUnitaire());
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
        BigDecimal totalMarchandise = scale2(nvl(reception.getTotalMarchandise()));
        BigDecimal totalFrais = scale2(nvl(reception.getTotalFrais()));

        if (totalFrais.compareTo(BigDecimal.ZERO) <= 0 || totalMarchandise.compareTo(BigDecimal.ZERO) <= 0) {
            for (ReceptionAchatLigne ligne : reception.getLignes()) {
                ligne.setPartFrais(BigDecimal.ZERO);
                ligne.setFraisUnitaire(BigDecimal.ZERO);
                ligne.setCoutUnitaireFinal(scale6(nvl(ligne.getPrixAchatUnitaire())));
            }
            return;
        }

        BigDecimal totalPartFraisAttribue = BigDecimal.ZERO;

        for (int i = 0; i < reception.getLignes().size(); i++) {
            ReceptionAchatLigne ligne = reception.getLignes().get(i);

            BigDecimal partFrais;

            if (i == reception.getLignes().size() - 1) {
                partFrais = totalFrais.subtract(totalPartFraisAttribue);
            } else {
                partFrais = ligne.getMontantAchat()
                        .multiply(totalFrais)
                        .divide(totalMarchandise, 6, RoundingMode.HALF_UP);
                partFrais = scale2(partFrais);
                totalPartFraisAttribue = totalPartFraisAttribue.add(partFrais);
            }

            BigDecimal qte = scale3(nvl(ligne.getQuantiteRecue()));
            BigDecimal fraisUnitaire = qte.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : partFrais.divide(qte, 6, RoundingMode.HALF_UP);

            BigDecimal coutFinal = scale6(nvl(ligne.getPrixAchatUnitaire()).add(fraisUnitaire));

            ligne.setPartFrais(scale2(partFrais));
            ligne.setFraisUnitaire(scale6(fraisUnitaire));
            ligne.setCoutUnitaireFinal(coutFinal);
        }
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
                            "CMD-" + reception.getCommandeAchat().getId()
                    )
            );
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

        return dto;
    }

    private BigDecimal totalFrais(ReceptionAchat r) {
        return scale2(
                nvl(r.getFraisTransport())
                        .add(nvl(r.getFraisDouane()))
                        .add(nvl(r.getFraisManutention()))
                        .add(nvl(r.getAutresFrais()))
        );
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