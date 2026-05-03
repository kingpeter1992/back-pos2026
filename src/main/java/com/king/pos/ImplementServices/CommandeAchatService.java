package com.king.pos.ImplementServices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.king.pos.Dao.CommandeAchatRepository;
import com.king.pos.Dao.FournisseurRepository;
import com.king.pos.Dao.ProduitRepository;
import com.king.pos.Dto.CommandeAchatLigneRequest;
import com.king.pos.Dto.CreateCommandeAchatRequest;
import com.king.pos.Dto.Response.CommandeAchatLigneResponse;
import com.king.pos.Dto.Response.CommandeAchatResponse;
import com.king.pos.Dto.Response.CommandeDashboardItemDto;
import com.king.pos.Dto.Response.CommandeDashboardResponse;
import com.king.pos.Dto.Response.FournisseurDashboardDto;
import com.king.pos.Entitys.CommandeAchat;
import com.king.pos.Entitys.CommandeAchatLigne;
import com.king.pos.Entitys.Fournisseur;
import com.king.pos.Entitys.Produit;
import com.king.pos.Interface.CommandeFournisseurService;
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

    // =========================================================
    // CREATE
    // =========================================================
    @Override
    public CommandeAchatResponse create(CreateCommandeAchatRequest request) {
        validateRequest(request);

        Fournisseur fournisseur = fournisseurRepository.findById(request.getFournisseurId())
                .orElseThrow(() -> new EntityNotFoundException("Fournisseur introuvable"));

        BigDecimal taux = getTauxRequest(request);

        CommandeAchat commande = new CommandeAchat();
        commande.setPrefixe(
                request.getPrefixe() != null && !request.getPrefixe().isBlank()
                        ? request.getPrefixe().trim()
                        : "CF"
        );

        commande.setRefCommande("TMP-" + UUID.randomUUID());
        commande.setDateCommande(request.getDateCommande() != null ? request.getDateCommande() : LocalDate.now());
        commande.setDateLivraisonPrevue(request.getDateLivraisonPrevue());
        commande.setFournisseur(fournisseur);
        commande.setDevise(Devise.CDF);
        commande.setTaux(taux);
        commande.setTauxChangeUtilise(taux);
        commande.setObservation(request.getObservation());
        commande.setStatut(StatutCommandeFournisseur.BROUILLON);

        List<CommandeAchatLigne> lignes = new ArrayList<>();

        for (CommandeAchatLigneRequest ligneRequest : request.getLignes()) {
            CommandeAchatLigne ligne = buildLigne(commande, ligneRequest, taux);
            lignes.add(ligne);
        }

        commande.setLignes(lignes);
        recalculerTotaux(commande);

        CommandeAchat saved = commandeAchatRepository.saveAndFlush(commande);

        saved.setRefCommande(buildReference(saved));
        saved = commandeAchatRepository.saveAndFlush(saved);

        return mapToResponse(saved);
    }

    // =========================================================
    // UPDATE
    // =========================================================
    @Override
    public CommandeAchatResponse update(Long id, CreateCommandeAchatRequest request) {
        if (id == null) {
            throw new IllegalStateException("L'identifiant de la commande est obligatoire.");
        }

        validateRequest(request);

        CommandeAchat commande = commandeAchatRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande achat introuvable : " + id));

        if (commande.getStatut() == StatutCommandeFournisseur.RECEPTIONNEE) {
            throw new IllegalStateException("Une commande réceptionnée ne peut pas être modifiée.");
        }

        boolean hasReception = commande.getLignes() != null && commande.getLignes().stream()
                .anyMatch(l -> nvl(l.getQuantiteRecue()).compareTo(BigDecimal.ZERO) > 0);

        if (hasReception) {
            throw new IllegalStateException("Impossible de modifier une commande ayant déjà des quantités reçues.");
        }

        Fournisseur fournisseur = fournisseurRepository.findById(request.getFournisseurId())
                .orElseThrow(() -> new EntityNotFoundException("Fournisseur introuvable"));

        BigDecimal taux = getTauxRequest(request);

        commande.setDateCommande(request.getDateCommande() != null ? request.getDateCommande() : commande.getDateCommande());
        commande.setDateLivraisonPrevue(request.getDateLivraisonPrevue());
        commande.setFournisseur(fournisseur);
        commande.setDevise(Devise.CDF);
        commande.setTaux(taux);
        commande.setTauxChangeUtilise(taux);
        commande.setObservation(request.getObservation());
        commande.setPrefixe(request.getPrefixe() != null ? request.getPrefixe() : commande.getPrefixe());

        commande.getLignes().clear();

        for (CommandeAchatLigneRequest ligneRequest : request.getLignes()) {
            CommandeAchatLigne ligne = buildLigne(commande, ligneRequest, taux);
            commande.getLignes().add(ligne);
        }

        if (commande.getStatut() == null) {
            commande.setStatut(StatutCommandeFournisseur.BROUILLON);
        }

        recalculerTotaux(commande);

        CommandeAchat updated = commandeAchatRepository.save(commande);
        return mapToResponse(updated);
    }

    // =========================================================
    // VALIDATION
    // =========================================================
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
            throw new IllegalStateException("Cette commande est déjà réceptionnée.");
        }

        commande.setStatut(StatutCommandeFournisseur.VALIDEE);

        CommandeAchat saved = commandeAchatRepository.save(commande);
        return mapToResponse(saved);
    }

    // =========================================================
    // FIND ALL
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public List<CommandeAchatResponse> findAll() {
        return commandeAchatRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // BUILD LIGNE
    // =========================================================
    private CommandeAchatLigne buildLigne(
            CommandeAchat commande,
            CommandeAchatLigneRequest ligneRequest,
            BigDecimal tauxCommande
    ) {
        if (ligneRequest.getProduitId() == null) {
            throw new IllegalStateException("Le produit est obligatoire pour chaque ligne.");
        }

        Produit produit = produitRepository.findById(ligneRequest.getProduitId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Produit introuvable : " + ligneRequest.getProduitId()
                ));

        BigDecimal quantite = nvl(ligneRequest.getQuantite());
        BigDecimal prixUnitaireFc = nvl(ligneRequest.getPrixUnitaireFc());
        BigDecimal prixUnitaire = nvl(ligneRequest.getPrixUnitaire());
        BigDecimal remiseFc = nvl(ligneRequest.getRemise());

        BigDecimal tauxLigne = nvl(ligneRequest.getTauxChangeUtilise());
        if (tauxLigne.compareTo(BigDecimal.ZERO) <= 0) {
            tauxLigne = tauxCommande;
        }

        if (prixUnitaireFc.compareTo(BigDecimal.ZERO) <= 0) {
            prixUnitaireFc = prixUnitaire;
        }

        if (quantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("La quantité commandée doit être > 0.");
        }

        if (prixUnitaireFc.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Le prix unitaire ne peut pas être négatif.");
        }

        if (remiseFc.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("La remise ne peut pas être négative.");
        }

        BigDecimal montantBrutFc = scale2(quantite.multiply(prixUnitaireFc));
        BigDecimal montantLigneFc = scale2(montantBrutFc.subtract(remiseFc));

        if (montantLigneFc.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("La remise ne peut pas être supérieure au montant de la ligne.");
        }

        BigDecimal prixUnitaireUsd = nvl(ligneRequest.getPrixUnitaireUsd());
        if (prixUnitaireUsd.compareTo(BigDecimal.ZERO) <= 0 && tauxLigne.compareTo(BigDecimal.ZERO) > 0) {
            prixUnitaireUsd = divUsd(prixUnitaireFc, tauxLigne);
        }

        BigDecimal montantLigneUsd = nvl(ligneRequest.getMontantLigneUsd());
        if (montantLigneUsd.compareTo(BigDecimal.ZERO) <= 0 && tauxLigne.compareTo(BigDecimal.ZERO) > 0) {
            montantLigneUsd = divUsd(montantLigneFc, tauxLigne);
        }

        CommandeAchatLigne ligne = new CommandeAchatLigne();
        ligne.setCommandeAchat(commande);
        ligne.setProduit(produit);

        ligne.setQuantiteCommandee(scale3(quantite));
        ligne.setPrixUnitaire(scale2(prixUnitaireFc));
        ligne.setRemise(scale2(remiseFc));
        ligne.setQuantiteRecue(BigDecimal.ZERO);

        ligne.setTauxChangeUtilise(scale6(tauxLigne));

        ligne.setPrixUnitaireFc(scale2(prixUnitaireFc));
        ligne.setPrixUnitaireUsd(scale2(prixUnitaireUsd));

        ligne.setMontantLigne(scale2(montantLigneFc));
        ligne.setMontantLigneFc(scale2(montantLigneFc));
        ligne.setMontantLigneUsd(scale2(montantLigneUsd));

        return ligne;
    }

    // =========================================================
    // RECALCUL TOTAUX
    // =========================================================
    private void recalculerTotaux(CommandeAchat commande) {
        BigDecimal totalBrutFc = BigDecimal.ZERO;
        BigDecimal totalRemiseFc = BigDecimal.ZERO;
        BigDecimal totalFc = BigDecimal.ZERO;
        BigDecimal totalUsd = BigDecimal.ZERO;

        for (CommandeAchatLigne ligne : commande.getLignes()) {
            BigDecimal quantite = nvl(ligne.getQuantiteCommandee());
            BigDecimal prixFc = nvl(ligne.getPrixUnitaireFc());
            BigDecimal remiseFc = nvl(ligne.getRemise());
            BigDecimal taux = nvl(ligne.getTauxChangeUtilise());

            BigDecimal brutFc = scale2(quantite.multiply(prixFc));
            BigDecimal netFc = scale2(brutFc.subtract(remiseFc));

            if (netFc.compareTo(BigDecimal.ZERO) < 0) {
                netFc = BigDecimal.ZERO;
            }

            BigDecimal prixUsd = BigDecimal.ZERO;
            BigDecimal netUsd = BigDecimal.ZERO;

            if (taux.compareTo(BigDecimal.ZERO) > 0) {
                prixUsd = divUsd(prixFc, taux);
                netUsd = divUsd(netFc, taux);
            }

            ligne.setPrixUnitaire(scale2(prixFc));
            ligne.setPrixUnitaireFc(scale2(prixFc));
            ligne.setPrixUnitaireUsd(scale2(prixUsd));
            ligne.setMontantLigne(scale2(netFc));
            ligne.setMontantLigneFc(scale2(netFc));
            ligne.setMontantLigneUsd(scale2(netUsd));

            totalBrutFc = totalBrutFc.add(brutFc);
            totalRemiseFc = totalRemiseFc.add(remiseFc);
            totalFc = totalFc.add(netFc);
            totalUsd = totalUsd.add(netUsd);
        }

        commande.setDevise(Devise.CDF);

        if (commande.getTauxChangeUtilise() == null || commande.getTauxChangeUtilise().compareTo(BigDecimal.ZERO) <= 0) {
            commande.setTauxChangeUtilise(nvl(commande.getTaux()));
        }

        commande.setMontantBrut(scale2(totalBrutFc));
        commande.setMontantRemise(scale2(totalRemiseFc));
        commande.setMontantTotal(scale2(totalFc));

        commande.setMontantTotalFc(scale2(totalFc));
        commande.setMontantTotalUsd(scale2(totalUsd));
    }

    // =========================================================
    // MAPPING RESPONSE
    // =========================================================
    private CommandeAchatResponse mapToResponse(CommandeAchat commande) {
        CommandeAchatResponse response = new CommandeAchatResponse();

        response.setId(commande.getId());
        response.setRefCommande(commande.getRefCommande());
        response.setDateCommande(commande.getDateCommande());
        response.setDateLivraisonPrevue(commande.getDateLivraisonPrevue());
        response.setDatePrevue(commande.getDateLivraisonPrevue());

        response.setDevise(commande.getDevise());
        response.setTaux(commande.getTaux());
        response.setTauxChangeUtilise(commande.getTauxChangeUtilise());

        response.setObservation(commande.getObservation());
        response.setStatut(commande.getStatut());

        response.setMontantBrut(commande.getMontantBrut());
        response.setMontantRemise(commande.getMontantRemise());
        response.setMontantTotal(commande.getMontantTotal());

        response.setMontantTotalFc(commande.getMontantTotalFc());
        response.setMontantTotalUsd(commande.getMontantTotalUsd());

        response.setPrefixe(commande.getPrefixe());

        Principal principal = null;
        String username = principal != null ? principal.getName() : "Anonyme";
        response.setUser(username);

        if (commande.getFournisseur() != null) {
            response.setFournisseurId(commande.getFournisseur().getId());
            response.setFournisseurNom(commande.getFournisseur().getNom());
        }

        List<CommandeAchatLigneResponse> ligneResponses = commande.getLignes()
                .stream()
                .map(this::mapLigneToResponse)
                .toList();

        response.setLignes(ligneResponses);

        return response;
    }

    private CommandeAchatLigneResponse mapLigneToResponse(CommandeAchatLigne ligne) {
        CommandeAchatLigneResponse lr = new CommandeAchatLigneResponse();

        lr.setId(ligne.getId());

        if (ligne.getProduit() != null) {
            lr.setProduitId(ligne.getProduit().getId());
            lr.setProduitNom(ligne.getProduit().getNom());
            lr.setCodeBarres(ligne.getProduit().getCodeBarres());
        }

        lr.setQuantiteCommandee(ligne.getQuantiteCommandee());
        lr.setQuantite(ligne.getQuantiteCommandee());
        lr.setQuantiteRecue(ligne.getQuantiteRecue());

        lr.setPrixUnitaire(ligne.getPrixUnitaire());
        lr.setRemise(ligne.getRemise());
        lr.setMontantLigne(ligne.getMontantLigne());

        lr.setTauxChangeUtilise(ligne.getTauxChangeUtilise());

        lr.setPrixUnitaireFc(ligne.getPrixUnitaireFc());
        lr.setPrixUnitaireUsd(ligne.getPrixUnitaireUsd());

        lr.setMontantLigneFc(ligne.getMontantLigneFc());
        lr.setMontantLigneUsd(ligne.getMontantLigneUsd());

        return lr;
    }

    // =========================================================
    // DASHBOARD
    // =========================================================
    @Override
    @Transactional(readOnly = true)
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

        BigDecimal montantTotalFc = BigDecimal.ZERO;
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

            BigDecimal montantCommandeFc = nvl(cmd.getMontantTotalFc());
            if (montantCommandeFc.compareTo(BigDecimal.ZERO) == 0) {
                montantCommandeFc = nvl(cmd.getMontantTotal());
            }

            montantTotalFc = montantTotalFc.add(montantCommandeFc);

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
            f.setMontantTotal(f.getMontantTotal().add(montantCommandeFc));
        }

        BigDecimal montantMoyenFc = montantTotalFc.divide(
                BigDecimal.valueOf(commandes.size()),
                2,
                RoundingMode.HALF_UP
        );

        BigDecimal tauxReception = BigDecimal.ZERO;
        if (quantiteTotaleCommandee.compareTo(BigDecimal.ZERO) > 0) {
            tauxReception = quantiteTotaleRecue
                    .multiply(BigDecimal.valueOf(100))
                    .divide(quantiteTotaleCommandee, 2, RoundingMode.HALF_UP);
        }

        List<CommandeDashboardItemDto> commandesRecentes = items.stream()
                .sorted(Comparator.comparing(
                        CommandeDashboardItemDto::getDateCommande,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(8)
                .collect(Collectors.toList());

        List<CommandeDashboardItemDto> commandesEnRetard = retards.stream()
                .sorted(Comparator.comparing(
                        CommandeDashboardItemDto::getJoursRetard,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
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

        response.setMontantTotal(montantTotalFc);
        response.setMontantMoyen(montantMoyenFc);

        response.setQuantiteTotaleCommandee(quantiteTotaleCommandee);
        response.setQuantiteTotaleRecue(quantiteTotaleRecue);
        response.setTauxReceptionGlobal(tauxReception);

        response.setCommandesRecentes(commandesRecentes);
        response.setCommandesEnRetard(commandesEnRetard);
        response.setTopFournisseurs(topFournisseurs);
        response.setAlertes(alertes);

        return response;
    }

    private CommandeDashboardItemDto mapToItem(
            CommandeAchat cmd,
            BigDecimal quantiteTotale,
            BigDecimal quantiteRecue,
            LocalDate today
    ) {
        CommandeDashboardItemDto dto = new CommandeDashboardItemDto();

        BigDecimal montantFc = nvl(cmd.getMontantTotalFc());
        if (montantFc.compareTo(BigDecimal.ZERO) == 0) {
            montantFc = nvl(cmd.getMontantTotal());
        }

        dto.setId(cmd.getId());
        dto.setRefCommande(cmd.getRefCommande());
        dto.setFournisseurNom(cmd.getFournisseur() != null ? cmd.getFournisseur().getNom() : null);
        dto.setDateCommande(cmd.getDateCommande());
        dto.setDatePrevue(cmd.getDateLivraisonPrevue());
        dto.setStatut(cmd.getStatut() != null ? cmd.getStatut().name() : null);
        dto.setMontantTotal(montantFc);
        dto.setDevise(Devise.CDF.name());

        dto.setQuantiteTotale(quantiteTotale);
        dto.setQuantiteRecue(quantiteRecue);

        BigDecimal progression = BigDecimal.ZERO;
        if (quantiteTotale.compareTo(BigDecimal.ZERO) > 0) {
            progression = quantiteRecue
                    .multiply(BigDecimal.valueOf(100))
                    .divide(quantiteTotale, 2, RoundingMode.HALF_UP);
        }

        dto.setProgression(progression);
        dto.setJoursRetard(computeJoursRetard(cmd, today));

        return dto;
    }

    private long computeJoursRetard(CommandeAchat cmd, LocalDate today) {
        if (cmd.getDateLivraisonPrevue() == null) return 0;

        String statut = safeUpper(cmd.getStatut() != null ? cmd.getStatut().name() : null);

        if ("LIVREE".equals(statut)
                || "CLOTUREE".equals(statut)
                || "ANNULEE".equals(statut)
                || "ANNULE".equals(statut)) {
            return 0;
        }

        if (cmd.getDateLivraisonPrevue().isBefore(today)) {
            return ChronoUnit.DAYS.between(cmd.getDateLivraisonPrevue(), today);
        }

        return 0;
    }

    private List<String> buildAlertes(
            long totalRetard,
            long totalBrouillon,
            long totalPartiel,
            BigDecimal tauxReception,
            int totalCommandes
    ) {
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

    // =========================================================
    // UTILS
    // =========================================================
    private void validateRequest(CreateCommandeAchatRequest request) {
        if (request == null) {
            throw new IllegalStateException("La requête est obligatoire.");
        }

        if (request.getFournisseurId() == null) {
            throw new IllegalStateException("Le fournisseur est obligatoire.");
        }

        if (request.getLignes() == null || request.getLignes().isEmpty()) {
            throw new IllegalStateException("La commande doit contenir au moins une ligne.");
        }
    }

    private BigDecimal getTauxRequest(CreateCommandeAchatRequest request) {
        BigDecimal taux = nvl(request.getTauxChangeUtilise());

        if (taux.compareTo(BigDecimal.ZERO) <= 0) {
            taux = nvl(request.getTaux());
        }

        if (taux.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Le taux de change est obligatoire.");
        }

        return scale6(taux);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
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

    private BigDecimal divUsd(BigDecimal montantFc, BigDecimal taux) {
        if (taux == null || taux.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return nvl(montantFc).divide(taux, 2, RoundingMode.HALF_UP);
    }

    private String buildReference(CommandeAchat commande) {
        if (commande.getId() == null) {
            throw new IllegalStateException("Impossible de générer la référence : ID null");
        }

        String prefixe = commande.getPrefixe() != null && !commande.getPrefixe().isBlank()
                ? commande.getPrefixe().trim()
                : "CF";

        return prefixe + "-" + String.format("%06d", commande.getId());
    }

    private String safeUpper(String value) {
        return value == null ? "" : value.toUpperCase(Locale.ROOT);
    }
}