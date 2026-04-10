package com.king.pos.ImplementServices;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.king.pos.Dao.CommandeAchatRepository;
import com.king.pos.Dao.DepotRepository;
import com.king.pos.Dao.FournisseurRepository;
import com.king.pos.Dao.MouvementStockRepository;
import com.king.pos.Dao.ProduitRepository;
import com.king.pos.Dao.ReceptionAchatRepository;
import com.king.pos.Dao.StockRepository;
import com.king.pos.Dto.CreateReceptionAchatRequest;
import com.king.pos.Dto.ReceptionAchatLigneRequest;
import com.king.pos.Dto.Response.ReceptionAchatLigneResponse;
import com.king.pos.Dto.Response.ReceptionAchatResponse;
import com.king.pos.Entitys.CommandeAchat;
import com.king.pos.Entitys.CommandeAchatLigne;
import com.king.pos.Entitys.Depot;
import com.king.pos.Entitys.Fournisseur;
import com.king.pos.Entitys.MouvementStock;
import com.king.pos.Entitys.Produit;
import com.king.pos.Entitys.ReceptionAchat;
import com.king.pos.Entitys.ReceptionAchatLigne;
import com.king.pos.Entitys.StockProduit;
import com.king.pos.enums.Devise;
import com.king.pos.enums.StatutCommandeFournisseur;
import com.king.pos.enums.StatutReceptionAchat;
import com.king.pos.enums.TypeMouvementStock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceptionAchatCreationService {

    private final ReceptionAchatRepository receptionAchatRepository;
    private final CommandeAchatRepository commandeAchatRepository;
    private final ProduitRepository produitRepository;
    private final FournisseurRepository fournisseurRepository;
    private final DepotRepository depotRepository;
    private final StockRepository stockRepository;
    private final MouvementStockRepository mouvementStockRepository;

    // =========================
    // CREATE (BROUILLON)
    // =========================
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
    reception.setDevise(request.getDevise() != null ? request.getDevise() : Devise.USD);
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

  //  return receptionAchatRepository.save(savedReception);
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

    if (reception == null) {
        throw new IllegalStateException("La réception est obligatoire.");
    }

    Depot depot = reception.getDepot();
    if (depot == null) {
        throw new IllegalStateException("Le dépôt de réception est obligatoire pour l'entrée en stock.");
    }

    BigDecimal quantiteRecue = scale3(nvl(ligne.getQuantiteRecue()));
    BigDecimal coutUnitaireFinal = scale6(nvl(ligne.getCoutUnitaireFinal()));
    BigDecimal prixUnitaireEntree = scale6(nvl(ligne.getCoutUnitaireFinal()));
    BigDecimal fraisUnitaire = scale6(nvl(ligne.getFraisUnitaire()));

    if (quantiteRecue.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalStateException("La quantité reçue doit être supérieure à zéro.");
    }

    if (coutUnitaireFinal.compareTo(BigDecimal.ZERO) < 0) {
        throw new IllegalStateException("Le coût unitaire final ne peut pas être négatif.");
    }

    StockProduit stock = stockRepository.findByProduitAndDepot(ligne.getProduit(), depot)
            .orElseGet(() -> stockRepository.save(
                    StockProduit.builder()
                            .produit(ligne.getProduit())
                            .depot(depot)
                            .quantiteDisponible(BigDecimal.ZERO)
                            .pmp(BigDecimal.ZERO)
                            .valeurStock(BigDecimal.ZERO)
                            .dateCreation(LocalDateTime.now())
                            .build()
            ));

    BigDecimal ancienStock = scale3(nvl(stock.getQuantiteDisponible()));
    BigDecimal ancienPmp = scale6(nvl(stock.getPmp()));

    BigDecimal valeurAncienne = ancienStock.multiply(ancienPmp);
    BigDecimal valeurNouvelleEntree = quantiteRecue.multiply(coutUnitaireFinal);

    BigDecimal nouveauStock = scale3(ancienStock.add(quantiteRecue));

    BigDecimal nouveauPmp = nouveauStock.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : valeurAncienne.add(valeurNouvelleEntree)
                    .divide(nouveauStock, 6, RoundingMode.HALF_UP);

    BigDecimal valeurStockApres = scale2(nouveauStock.multiply(nouveauPmp));

    stock.setQuantiteDisponible(nouveauStock);
    stock.setPmp(scale6(nouveauPmp));
    stock.setValeurStock(valeurStockApres);
    stock.setDateDerniereEntree(LocalDateTime.now());

    stockRepository.save(stock);

    mouvementStockRepository.save(
            MouvementStock.builder()
                    .produit(ligne.getProduit())
                    .depot(depot)
                    .typeMouvement(TypeMouvementStock.ENTREE_ACHAT)
                    .quantite(quantiteRecue)
                    .prixUnitaireEntree(prixUnitaireEntree)
                    .fraisUnitaire(fraisUnitaire)
                    .coutUnitaireFinal(coutUnitaireFinal)
                    .ancienStock(ancienStock)
                    .ancienPmp(ancienPmp)
                    .nouveauStock(nouveauStock)
                    .nouveauPmp(scale6(nouveauPmp))
                    .referenceDocument(reception.getRefReception())
                    .libelle(buildLibelleEntree(reception, ligne, ancienStock, nouveauStock))
                    .dateMouvement(LocalDateTime.now())
                    .build()
    );
}

private String buildLibelleEntree(ReceptionAchat reception, ReceptionAchatLigne ligne, BigDecimal ancienStock, BigDecimal nouveauStock) {
    return String.format(
            "Entrée en stock - Réception: %s, Produit: %s, Qté: %s, Ancien Stock: %s -> Nouveau Stock: %s",
            safeString(reception.getRefReception()),
            safeString(ligne.getProduit().getNom()),
            scale3(ligne.getQuantiteRecue()),
            scale3(ancienStock),
            scale3(nouveauStock)
    );
}

private void sortieStock(
        Produit produit,
        Depot depot,
        BigDecimal quantiteSortie,
        TypeMouvementStock typeMouvement,
        String referenceDocument,
        String libelle
) {
    if (produit == null) {
        throw new IllegalStateException("Le produit est obligatoire pour la sortie de stock.");
    }

    if (depot == null) {
        throw new IllegalStateException("Le dépôt est obligatoire pour la sortie de stock.");
    }

    if (typeMouvement == null) {
        throw new IllegalStateException("Le type de mouvement est obligatoire.");
    }

    BigDecimal quantite = scale3(nvl(quantiteSortie));
    if (quantite.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalStateException("La quantité de sortie doit être supérieure à zéro.");
    }

    StockProduit stock = stockRepository.findByProduitAndDepot(produit, depot)
            .orElseThrow(() -> new IllegalStateException(
                    "Aucun stock trouvé pour le produit '" + produit.getNom() + "' dans le dépôt '" + depot.getNom() + "'."
            ));

    BigDecimal ancienStock = scale3(nvl(stock.getQuantiteDisponible()));
    BigDecimal ancienPmp = scale6(nvl(stock.getPmp()));

    if (ancienStock.compareTo(quantite) < 0) {
        throw new IllegalStateException(
                "Stock insuffisant pour le produit '" + produit.getNom()
                        + "'. Stock disponible: " + ancienStock
                        + ", quantité demandée: " + quantite + "."
        );
    }

    BigDecimal nouveauStock = scale3(ancienStock.subtract(quantite));
    BigDecimal nouveauPmp = ancienPmp; // en sortie, le PMP reste inchangé
    BigDecimal nouvelleValeurStock = scale2(nouveauStock.multiply(nouveauPmp));

    stock.setQuantiteDisponible(nouveauStock);
    stock.setPmp(nouveauPmp);
    stock.setValeurStock(nouvelleValeurStock);
    stock.setDateDerniereSortie(LocalDateTime.now());

    stockRepository.save(stock);

    mouvementStockRepository.save(
            MouvementStock.builder()
                    .produit(produit)
                    .depot(depot)
                    .typeMouvement(typeMouvement)
                    .quantite(quantite)
                    .prixUnitaireEntree(BigDecimal.ZERO)
                    .fraisUnitaire(BigDecimal.ZERO)
                    .coutUnitaireFinal(scale6(nouveauPmp))
                    .ancienStock(ancienStock)
                    .ancienPmp(ancienPmp)
                    .nouveauStock(nouveauStock)
                    .nouveauPmp(nouveauPmp)
                    .referenceDocument(referenceDocument)
                    .libelle(resolveLibelleSortie(libelle, produit, quantite, ancienStock, nouveauStock))
                    .dateMouvement(LocalDateTime.now())
                    .build()
    );
}


private String resolveLibelleSortie(
        String libelle,
        Produit produit,
        BigDecimal quantite,
        BigDecimal ancienStock,
        BigDecimal nouveauStock
) {
    if (libelle != null && !libelle.trim().isEmpty()) {
        return libelle;
    }

    return "Sortie stock"
            + " | Produit: " + safeString(produit != null ? produit.getNom() : null)
            + " | Qté: " + scale3(nvl(quantite))
            + " | Stock avant: " + scale3(nvl(ancienStock))
            + " | Stock après: " + scale3(nvl(nouveauStock));
}
private String safeString(String value) {
    return value != null ? value : "";
}

private BigDecimal nvl(BigDecimal v) {
    return v == null ? BigDecimal.ZERO : v;
}

private BigDecimal totalFrais(ReceptionAchat r) {
    return scale2(
            nvl(r.getFraisTransport())
                    .add(nvl(r.getFraisDouane()))
                    .add(nvl(r.getFraisManutention()))
                    .add(nvl(r.getAutresFrais()))
    );
}

private BigDecimal scale2(BigDecimal v) {
    return nvl(v).setScale(2, RoundingMode.HALF_UP);
}

private BigDecimal scale3(BigDecimal v) {
    return nvl(v).setScale(3, RoundingMode.HALF_UP);
}

private BigDecimal scale6(BigDecimal v) {
    return nvl(v).setScale(6, RoundingMode.HALF_UP);
}

/* private ReceptionAchatResponse mapToResponse(ReceptionAchat reception) {
    ReceptionAchatResponse response = new ReceptionAchatResponse();
    response.setId(reception.getId());
    response.setRefReception(reception.getRefReception());
    response.setDateReception(reception.getDateReception());
    response.setStatut(reception.getStatut() != null ? reception.getStatut().name() : null);

    if (reception.getDepot() != null) {
        response.setDepotId(reception.getDepot().getId());
        response.setDepotNom(reception.getDepot().getNom());
    }

    if (reception.getFournisseur() != null) {
        response.setFournisseurId(reception.getFournisseur().getId());
        response.setFournisseurNom(reception.getFournisseur().getNom());
    }

    if (reception.getCommandeAchat() != null) {
        response.setCommandeAchatId(reception.getCommandeAchat().getId());
        response.setRefCommande(reception.getCommandeAchat().getRefCommande());
    }

    response.setTotalMarchandise(reception.getTotalMarchandise());
    response.setTotalFrais(reception.getTotalFrais());
    response.setTotalGeneral(reception.getTotalGeneral());

    List<ReceptionAchatLigneResponse> lignes = reception.getLignes().stream().map(l -> {
        ReceptionAchatLigneResponse lr = new ReceptionAchatLigneResponse();
        lr.setId(l.getId());

        if (l.getProduit() != null) {
            lr.setProduitId(l.getProduit().getId());
            lr.setProduitNom(l.getProduit().getNom());

            if (l.getProduit().getCategorie() != null) {
                lr.setCategorieId(l.getProduit().getCategorie().getId());
                lr.setCategorieNom(l.getProduit().getCategorie().getNom());
            }
        }

        lr.setQuantiteRecue(l.getQuantiteRecue());
        lr.setPrixAchatUnitaire(l.getPrixAchatUnitaire());
        lr.setMontantAchat(l.getMontantAchat());
        lr.setPartFrais(l.getPartFrais());
        lr.setFraisUnitaire(l.getFraisUnitaire());
        lr.setCoutUnitaireFinal(l.getCoutUnitaireFinal());
        return lr;
    }).toList();

    response.setLignes(lignes);

    return response;
}
 */
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
            // la dernière ligne prend le reliquat pour éviter l'écart d'arrondi
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

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String valueOrDefault(String value, String defaultValue) {
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

}