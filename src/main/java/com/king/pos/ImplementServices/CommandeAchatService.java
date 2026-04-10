package com.king.pos.ImplementServices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.king.pos.Dao.CommandeAchatRepository;
import com.king.pos.Dao.FournisseurRepository;
import com.king.pos.Dao.ProduitRepository;
import com.king.pos.Dto.CommandeAchatLigneRequest;
import com.king.pos.Dto.CreateCommandeAchatRequest;
import com.king.pos.Dto.LigneCommandeRequest;
import com.king.pos.Dto.ReceptionLigneRequest;
import com.king.pos.Dto.Response.CommandeAchatLigneResponse;
import com.king.pos.Dto.Response.CommandeAchatResponse;
import com.king.pos.Dto.Response.CommandeDashboardItemDto;
import com.king.pos.Dto.Response.CommandeDashboardResponse;
import com.king.pos.Dto.Response.FournisseurDashboardDto;
import com.king.pos.Entitys.CommandeAchat;
import com.king.pos.Entitys.Fournisseur;
import com.king.pos.Entitys.CommandeAchatLigne;
import com.king.pos.Entitys.Produit;
import com.king.pos.Entitys.ReceptionAchatLigne;
import com.king.pos.Handllers.BusinessException;
import com.king.pos.Handllers.ResourceNotFoundException;
import com.king.pos.Interface.CommandeFournisseurService;
import com.king.pos.Interface.StockService;
import com.king.pos.enums.Devise;
import com.king.pos.enums.StatutCommandeFournisseur;

import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommandeAchatService implements CommandeFournisseurService {

    private final CommandeAchatRepository commandeAchatRepository;
    private final FournisseurRepository fournisseurRepository;
    private final ProduitRepository produitRepository;

@Transactional
@Override
public CommandeAchatResponse create(CreateCommandeAchatRequest request) {
    if (request.getFournisseurId() == null) {
        throw new IllegalStateException("Le fournisseur est obligatoire.");
    }

    if (request.getLignes() == null || request.getLignes().isEmpty()) {
        throw new IllegalStateException("La commande doit contenir au moins une ligne.");
    }

    Fournisseur fournisseur = fournisseurRepository.findById(request.getFournisseurId())
            .orElseThrow(() -> new EntityNotFoundException("Fournisseur introuvable"));

    CommandeAchat commande = new CommandeAchat();
    commande.setPrefixe(
            request.getPrefixe() != null && !request.getPrefixe().isBlank()
                    ? request.getPrefixe().trim()
                    : "CF"
    );
    commande.setDateCommande(request.getDateCommande() != null ? request.getDateCommande() : LocalDate.now());
    commande.setDateLivraisonPrevue(request.getDateLivraisonPrevue());
    commande.setFournisseur(fournisseur);
    commande.setDevise(request.getDevise() != null ? request.getDevise() : Devise.USD);
    commande.setTaux(request.getTaux() != null ? request.getTaux() : BigDecimal.ONE);
    commande.setObservation(request.getObservation());
    commande.setStatut(StatutCommandeFournisseur.BROUILLON);

    List<CommandeAchatLigne> lignes = new ArrayList<>();
    BigDecimal totalBrut = BigDecimal.ZERO;
    BigDecimal totalRemise = BigDecimal.ZERO;

    for (CommandeAchatLigneRequest ligneRequest : request.getLignes()) {
        Produit produit = produitRepository.findById(ligneRequest.getProduitId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Produit introuvable : " + ligneRequest.getProduitId()));

        BigDecimal quantite = nvl(ligneRequest.getQuantite());
        BigDecimal prix = nvl(ligneRequest.getPrixUnitaire());
        BigDecimal remise = nvl(ligneRequest.getRemise());

        if (quantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("La quantité commandée doit être > 0.");
        }

        if (prix.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Le prix unitaire ne peut pas être négatif.");
        }

        if (remise.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("La remise ne peut pas être négative.");
        }

        BigDecimal montantBrutLigne = scale2(quantite.multiply(prix));
        BigDecimal montantNetLigne = scale2(montantBrutLigne.subtract(remise));

        if (montantNetLigne.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("La remise ne peut pas être supérieure au montant de la ligne.");
        }

        CommandeAchatLigne ligne = new CommandeAchatLigne();
        ligne.setCommandeAchat(commande);
        ligne.setProduit(produit);
        ligne.setQuantiteCommandee(scale3(quantite));
        ligne.setPrixUnitaire(scale2(prix));
        ligne.setRemise(scale2(remise));
        ligne.setQuantiteRecue(BigDecimal.ZERO);
        ligne.setMontantLigne(montantNetLigne);

        totalBrut = totalBrut.add(montantBrutLigne);
        totalRemise = totalRemise.add(remise);
        lignes.add(ligne);
    }

    commande.setLignes(lignes);
    commande.setMontantBrut(scale2(totalBrut));
    commande.setMontantRemise(scale2(totalRemise));
    commande.setMontantTotal(scale2(totalBrut.subtract(totalRemise)));

    // Référence temporaire unique pour satisfaire NOT NULL + UNIQUE
    commande.setRefCommande("TMP-" + UUID.randomUUID());

    // 1er save pour obtenir l'ID
    CommandeAchat saved = commandeAchatRepository.saveAndFlush(commande);

    // Génération de la vraie référence métier
    String ref = buildReference(saved);
    saved.setRefCommande(ref);

    // 2e save avec la vraie référence
    saved = commandeAchatRepository.saveAndFlush(saved);

    return mapToResponse(saved);
}
private String buildReference(CommandeAchat commande) {
    if (commande.getId() == null) {
        throw new IllegalStateException("Impossible de générer la référence : ID null");
    }

    String prefixe = (commande.getPrefixe() != null && !commande.getPrefixe().isBlank())
            ? commande.getPrefixe().trim()
            : "CF";

    return prefixe + "-" + String.format("%06d", commande.getId());
}

    @Transactional
    @Override
    public CommandeAchatResponse valider(Long id) {
        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande introuvable"));

        if (commande.getLignes() == null || commande.getLignes().isEmpty()) {
            throw new IllegalStateException("Impossible de valider une commande vide.");
        }

        if (commande.getStatut() == StatutCommandeFournisseur.ANNULEE) {
            throw new IllegalStateException("Cette commande est annulée.");
        }

        if (commande.getStatut() == StatutCommandeFournisseur.RECEPTIONNEE) {
            throw new IllegalStateException("Cette commande est annulée.");
        }

        commande.setStatut(StatutCommandeFournisseur.VALIDEE);
        commandeAchatRepository.save(commande);
        return mapToResponse(commande);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal scale2(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scale3(BigDecimal value) {
        return value.setScale(3, RoundingMode.HALF_UP);
    }

    private CommandeAchatResponse mapToResponse(CommandeAchat commande) {
        CommandeAchatResponse response = new CommandeAchatResponse();
        response.setId(commande.getId());
        response.setRefCommande(commande.getRefCommande());
        response.setDateCommande(commande.getDateCommande());
        response.setDateLivraisonPrevue(commande.getDateLivraisonPrevue());
        response.setDevise(commande.getDevise());
        response.setTaux(commande.getTaux());
        response.setObservation(commande.getObservation());
        response.setStatut(commande.getStatut());
        response.setMontantTotal(commande.getMontantTotal());
        response.setMontantBrut(commande.getMontantBrut());
        response.setMontantRemise(commande.getMontantRemise());
        response.setPrefixe(commande.getPrefixe());
        response.setDatePrevue(commande.getDateLivraisonPrevue());
        Principal principal = null; // Récupérer le principal de la sécurité contextuelle
        String username = principal != null ? principal.getName() : "Anonyme";
        response.setUser(username);
     



        if (commande.getFournisseur() != null) {
            response.setFournisseurId(commande.getFournisseur().getId());
            response.setFournisseurNom(commande.getFournisseur().getNom());
        }

        List<CommandeAchatLigneResponse> ligneResponses = commande.getLignes().stream().map(ligne -> {
            CommandeAchatLigneResponse lr = new CommandeAchatLigneResponse();
            lr.setId(ligne.getId());
            lr.setProduitId(ligne.getProduit() != null ? ligne.getProduit().getId() : null);
            lr.setProduitNom(ligne.getProduit() != null ? ligne.getProduit().getNom() : null);
            lr.setCodeBarres(ligne.getProduit() != null ? ligne.getProduit().getCodeBarres() : null);
            lr.setQuantiteCommandee(ligne.getQuantiteCommandee());
            lr.setQuantiteRecue(ligne.getQuantiteRecue());
            lr.setPrixUnitaire(ligne.getPrixUnitaire());
            lr.setMontantLigne(ligne.getMontantLigne());
            return lr;
        }).toList();

        response.setLignes(ligneResponses);

        return response;
    }

    @Transactional
    @Override
    public CommandeAchatResponse update(Long id, CreateCommandeAchatRequest request) {
        if (id == null) {
            throw new IllegalStateException("L'identifiant de la commande est obligatoire.");
        }

        if (request.getFournisseurId() == null) {
            throw new IllegalStateException("Le fournisseur est obligatoire.");
        }

        if (request.getLignes() == null || request.getLignes().isEmpty()) {
            throw new IllegalStateException("La commande doit contenir au moins une ligne.");
        }

        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande achat introuvable : " + id));

        // règle métier éventuelle
        if (commande.getStatut() == StatutCommandeFournisseur.RECEPTIONNEE) {
            throw new IllegalStateException("Une commande clôturée ne peut pas être modifiée.");
        }

        Fournisseur fournisseur = fournisseurRepository.findById(request.getFournisseurId())
                .orElseThrow(() -> new EntityNotFoundException("Fournisseur introuvable"));

        commande.setDateCommande(
                request.getDateCommande() != null ? request.getDateCommande() : commande.getDateCommande());
        commande.setDateLivraisonPrevue(request.getDateLivraisonPrevue());
        commande.setFournisseur(fournisseur);
        commande.setDevise(request.getDevise() != null ? request.getDevise() : Devise.USD);
        commande.setTaux(request.getTaux() != null ? request.getTaux() : BigDecimal.ONE);
        commande.setObservation(request.getObservation());
        commande.setPrefixe(request.getPrefixe() != null ? request.getPrefixe() : commande.getPrefixe());

      
        // On protège les commandes déjà réceptionnées partiellement/totalement
        boolean hasReception = commande.getLignes() != null && commande.getLignes().stream()
                .anyMatch(l -> nvl(l.getQuantiteRecue()).compareTo(BigDecimal.ZERO) > 0);

        if (hasReception) {
            throw new IllegalStateException("Impossible de modifier une commande ayant déjà des quantités reçues.");
        }

        List<CommandeAchatLigne> nouvellesLignes = new ArrayList<>();
        BigDecimal totalBrut = BigDecimal.ZERO;
        BigDecimal totalRemise = BigDecimal.ZERO;

        for (CommandeAchatLigneRequest ligneRequest : request.getLignes()) {
            if (ligneRequest.getProduitId() == null) {
                throw new IllegalStateException("Le produit est obligatoire pour chaque ligne.");
            }

            Produit produit = produitRepository.findById(ligneRequest.getProduitId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Produit introuvable : " + ligneRequest.getProduitId()));

            BigDecimal quantite = nvl(ligneRequest.getQuantite());
            BigDecimal prix = nvl(ligneRequest.getPrixUnitaire());
            BigDecimal remise = nvl(ligneRequest.getRemise());

            if (quantite.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("La quantité commandée doit être > 0.");
            }

            if (prix.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException("Le prix unitaire ne peut pas être négatif.");
            }

            if (remise.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException("La remise ne peut pas être négative.");
            }

            BigDecimal montantBrutLigne = scale2(quantite.multiply(prix));
            BigDecimal montantNetLigne = scale2(montantBrutLigne.subtract(remise));

            if (montantNetLigne.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException("La remise ne peut pas être supérieure au montant de la ligne.");
            }

            CommandeAchatLigne ligne = new CommandeAchatLigne();
            ligne.setCommandeAchat(commande);
            ligne.setProduit(produit);
            ligne.setQuantiteCommandee(scale3(quantite));
            ligne.setPrixUnitaire(scale2(prix));
            ligne.setRemise(scale2(remise));
            ligne.setQuantiteRecue(BigDecimal.ZERO);
            ligne.setMontantLigne(montantNetLigne);

            totalBrut = totalBrut.add(montantBrutLigne);
            totalRemise = totalRemise.add(remise);
            nouvellesLignes.add(ligne);
        }

        // Remplacement complet des lignes
        commande.getLignes().clear();
        commande.getLignes().addAll(nouvellesLignes);

        commande.setMontantBrut(scale2(totalBrut));
        commande.setMontantRemise(scale2(totalRemise));
        commande.setMontantTotal(scale2(totalBrut.subtract(totalRemise)));

        // si aucune position fournie, on garde BROUILLON ou l'état existant
        if (commande.getStatut() == null) {
            commande.setStatut(StatutCommandeFournisseur.BROUILLON);
        }

        CommandeAchat updated = commandeAchatRepository.save(commande);
        return mapToResponse(updated);
    }
@Override
    public List<CommandeAchatResponse> findAll() {
        // TODO Auto-generated method stub
        return commandeAchatRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CommandeDashboardResponse getDashboard() {
        List<CommandeAchat> commandes = commandeAchatRepository.findAll();
        CommandeDashboardResponse response = new CommandeDashboardResponse();

        if (commandes == null || commandes.isEmpty()) {
            response.getAlertes().add("Aucune commande disponible.");
            return response;
        }

        LocalDate today = LocalDate.now();

        long totalBrouillon = 0;
        long totalEnCours = 0;
        long totalPartiel = 0;
        long totalLivre = 0;
        long totalAnnule = 0;
        long totalRetard = 0;

        BigDecimal montantTotal = BigDecimal.ZERO;
        BigDecimal quantiteTotaleCommandee = BigDecimal.ZERO;
        BigDecimal quantiteTotaleRecue = BigDecimal.ZERO;

        Map<Long, FournisseurDashboardDto> topFournisseursMap = new HashMap<>();

        List<CommandeDashboardItemDto> items = new ArrayList<>();
        List<CommandeDashboardItemDto> retards = new ArrayList<>();

        for (CommandeAchat cmd : commandes) {
            String statut = safeUpper(cmd.getStatut() != null ? cmd.getStatut().name() : null);

            if ("BROUILLON".equals(statut)) totalBrouillon++;
            if ("EN_COURS".equals(statut) || "VALIDEE".equals(statut)) totalEnCours++;
            if (statut.contains("PARTIEL")) totalPartiel++;
            if ("LIVREE".equals(statut) || "CLOTUREE".equals(statut)) totalLivre++;
            if ("ANNULEE".equals(statut) || "ANNULE".equals(statut)) totalAnnule++;

            BigDecimal montantCommande = nvl(cmd.getMontantTotal());
            montantTotal = montantTotal.add(montantCommande);

            BigDecimal qteCmd = BigDecimal.ZERO;
            BigDecimal qteRecue = BigDecimal.ZERO;

            if (cmd.getLignes() != null) {
                for (CommandeAchatLigne ligne : cmd.getLignes()) {
                    qteCmd = qteCmd.add(nvl(ligne.getQuantiteCommandee()));
                    qteRecue = qteRecue.add(nvl(ligne.getQuantiteRecue()));
                }
            }

            quantiteTotaleCommandee = quantiteTotaleCommandee.add(qteCmd);
            quantiteTotaleRecue = quantiteTotaleRecue.add(qteRecue);

            CommandeDashboardItemDto dto = mapToItem(cmd, qteCmd, qteRecue, today);
            items.add(dto);

            if (dto.getJoursRetard() != null && dto.getJoursRetard() > 0) {
                totalRetard++;
                retards.add(dto);
            }

            Long fournisseurId = cmd.getFournisseur() != null ? cmd.getFournisseur().getId() : 0L;
            String fournisseurNom = cmd.getFournisseur() != null ? cmd.getFournisseur().getNom() : "Non défini";

            FournisseurDashboardDto f = topFournisseursMap.get(fournisseurId);
            if (f == null) {
                f = new FournisseurDashboardDto();
                f.setFournisseurId(fournisseurId);
                f.setFournisseurNom(fournisseurNom);
                f.setTotalCommandes(0);
                f.setMontantTotal(BigDecimal.ZERO);
                topFournisseursMap.put(fournisseurId, f);
            }

            f.setTotalCommandes(f.getTotalCommandes() + 1);
            f.setMontantTotal(f.getMontantTotal().add(montantCommande));
        }

        BigDecimal montantMoyen = BigDecimal.ZERO;
        if (!commandes.isEmpty()) {
            montantMoyen = montantTotal.divide(
                    BigDecimal.valueOf(commandes.size()),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        BigDecimal tauxReception = BigDecimal.ZERO;
        if (quantiteTotaleCommandee.compareTo(BigDecimal.ZERO) > 0) {
            tauxReception = quantiteTotaleRecue
                    .multiply(BigDecimal.valueOf(100))
                    .divide(quantiteTotaleCommandee, 2, RoundingMode.HALF_UP);
        }

        List<CommandeDashboardItemDto> commandesRecentes = items.stream()
                .sorted(Comparator.comparing(CommandeDashboardItemDto::getDateCommande,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(8)
                .collect(Collectors.toList());

        List<CommandeDashboardItemDto> commandesEnRetard = retards.stream()
                .sorted(Comparator.comparing(CommandeDashboardItemDto::getJoursRetard,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .collect(Collectors.toList());

        List<FournisseurDashboardDto> topFournisseurs = topFournisseursMap.values().stream()
                .sorted((a, b) -> b.getMontantTotal().compareTo(a.getMontantTotal()))
                .limit(5)
                .collect(Collectors.toList());

        List<String> alertes = buildAlertes(
                totalRetard,
                totalBrouillon,
                totalPartiel,
                tauxReception,
                commandes.size()
        );

        response.setTotalCommandes(commandes.size());
        response.setTotalBrouillon(totalBrouillon);
        response.setTotalEnCours(totalEnCours);
        response.setTotalPartielLivre(totalPartiel);
        response.setTotalLivre(totalLivre);
        response.setTotalAnnule(totalAnnule);
        response.setTotalRetard(totalRetard);

        response.setMontantTotal(montantTotal);
        response.setMontantMoyen(montantMoyen);

        response.setQuantiteTotaleCommandee(quantiteTotaleCommandee);
        response.setQuantiteTotaleRecue(quantiteTotaleRecue);
        response.setTauxReceptionGlobal(tauxReception);

        response.setCommandesRecentes(commandesRecentes);
        response.setCommandesEnRetard(commandesEnRetard);
        response.setTopFournisseurs(topFournisseurs);
        response.setAlertes(alertes);

        return response;
    }

    private CommandeDashboardItemDto mapToItem(CommandeAchat cmd,
                                               BigDecimal quantiteTotale,
                                               BigDecimal quantiteRecue,
                                               LocalDate today) {
        CommandeDashboardItemDto dto = new CommandeDashboardItemDto();

        dto.setId(cmd.getId());
        dto.setRefCommande(cmd.getRefCommande());
        dto.setFournisseurNom(cmd.getFournisseur() != null ? cmd.getFournisseur().getNom() : null);
        dto.setDateCommande(cmd.getDateCommande());
        dto.setDatePrevue(cmd.getDateLivraisonPrevue());
        dto.setStatut(cmd.getStatut() != null ? cmd.getStatut().name() : null);
        dto.setMontantTotal(nvl(cmd.getMontantTotal()));
        dto.setDevise(cmd.getDevise() != null ? cmd.getDevise().name() : null);

        dto.setQuantiteTotale(quantiteTotale);
        dto.setQuantiteRecue(quantiteRecue);

        BigDecimal progression = BigDecimal.ZERO;
        if (quantiteTotale.compareTo(BigDecimal.ZERO) > 0) {
            progression = quantiteRecue
                    .multiply(BigDecimal.valueOf(100))
                    .divide(quantiteTotale, 2, RoundingMode.HALF_UP);
        }
        dto.setProgression(progression);

        long joursRetard = computeJoursRetard(cmd, today);
        dto.setJoursRetard(joursRetard);

        return dto;
    }

    private long computeJoursRetard(CommandeAchat cmd, LocalDate today) {
        if (cmd.getDateLivraisonPrevue() == null) return 0;

        String statut = safeUpper(cmd.getStatut() != null ? cmd.getStatut().name() : null);
        if ("LIVREE".equals(statut) || "CLOTUREE".equals(statut) || "ANNULEE".equals(statut) || "ANNULE".equals(statut)) {
            return 0;
        }

        if (cmd.getDateLivraisonPrevue().isBefore(today)) {
            return ChronoUnit.DAYS.between(cmd.getDateLivraisonPrevue(), today);
        }

        return 0;
    }

    private List<String> buildAlertes(long totalRetard,
                                      long totalBrouillon,
                                      long totalPartiel,
                                      BigDecimal tauxReception,
                                      int totalCommandes) {
        List<String> list = new ArrayList<>();

        if (totalCommandes == 0) {
            list.add("Aucune commande disponible.");
            return list;
        }

        if (totalRetard > 0) {
            list.add(totalRetard + " commande(s) en retard de livraison.");
        }

        if (totalBrouillon > 0) {
            list.add(totalBrouillon + " commande(s) encore en brouillon.");
        }

        if (totalPartiel > 0) {
            list.add(totalPartiel + " commande(s) partiellement livrées.");
        }

        if (tauxReception.compareTo(BigDecimal.valueOf(50)) < 0) {
            list.add("Le taux global de réception est faible (" + tauxReception + "%).");
        }

        if (list.isEmpty()) {
            list.add("Aucune alerte critique détectée.");
        }

        return list;
    }

   
    private String safeUpper(String value) {
        return value == null ? "" : value.toUpperCase(Locale.ROOT);
    }
}
