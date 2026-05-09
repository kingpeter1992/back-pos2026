package com.king.pos.ImplementServices;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.king.pos.Dao.DepotRepository;
import com.king.pos.Dao.HistoriqueRepository;
import com.king.pos.Dao.LocatorRepository;
import com.king.pos.Dao.ProduitRepository;
import com.king.pos.Dao.RequisitionProduitRepository;
import com.king.pos.Dao.RequisitionResponse;
import com.king.pos.Dto.TypeRequisition;
import com.king.pos.Entitys.Depot;
import com.king.pos.Entitys.Locator;
import com.king.pos.Entitys.Produit;
import com.king.pos.Entitys.RequisitionHistorique;
import com.king.pos.Entitys.RequisitionProduit;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RequisitionService {

    private final ProduitRepository produitRepository;
    private final DepotRepository depotRepository;
    private final LocatorRepository locatorRepository;
    private final RequisitionProduitRepository requisitionRepository;
    private final HistoriqueRepository historiqueRepository;

    public void enregistrerDemande(
            Long produitId,
            Long depotId,
            Long locatorId,
            BigDecimal quantite,
            TypeRequisition type,
            String user
    ) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable : " + produitId));

        Depot depot = depotId != null
                ? depotRepository.findById(depotId).orElse(null)
                : null;

        Locator locator = locatorId != null
                ? locatorRepository.findById(locatorId).orElse(null)
                : null;

        BigDecimal qte = nvl(quantite);

        boolean venteReelle = type == TypeRequisition.VENTE_REALISEE;
        boolean venteManquee = type == TypeRequisition.VENTE_MANQUEE
                || type == TypeRequisition.NOUVEAU_PRODUIT_DEMANDE;

        boolean produitExistant = type != TypeRequisition.NOUVEAU_PRODUIT_DEMANDE;

        RequisitionHistorique hist = RequisitionHistorique.builder()
                .produit(produit)
                .depot(depot)
                .locator(locator)
                .type(type)
                .quantiteDemandee(venteManquee ? qte : BigDecimal.ZERO)
                .quantiteVendue(venteReelle ? qte : BigDecimal.ZERO)
                .nombreDemandes(1)
                .nombreVentes(venteReelle ? 1 : 0)
                .nombreVentesManquees(venteManquee ? 1 : 0)
                .produitExistant(produitExistant)
                .origine(type.name())
                .date(LocalDate.now())
                .dateHeure(LocalDateTime.now())
                .creePar(user)
                .build();

        historiqueRepository.save(hist);

        RequisitionProduit resume = requisitionRepository
                .findByProduitIdAndDepotIdAndLocatorId(produitId, depotId, locatorId)
                .orElseGet(() -> RequisitionProduit.builder()
                        .produit(produit)
                        .depot(depot)
                        .locator(locator)
                        .totalDemandes(0)
                        .totalVentes(0)
                        .totalVentesManquees(0)
                        .totalQuantiteVendue(BigDecimal.ZERO)
                        .totalQuantiteDemandeeNonVendue(BigDecimal.ZERO)
                        .produitExistant(produitExistant)
                        .origine(type.name())
                        .build());

        resume.setTotalDemandes(nvlInt(resume.getTotalDemandes()) + 1);

        if (venteReelle) {
            resume.setTotalVentes(nvlInt(resume.getTotalVentes()) + 1);
            resume.setTotalQuantiteVendue(
                    nvl(resume.getTotalQuantiteVendue()).add(qte)
            );
        }

        if (venteManquee) {
            resume.setTotalVentesManquees(nvlInt(resume.getTotalVentesManquees()) + 1);
            resume.setTotalQuantiteDemandeeNonVendue(
                    nvl(resume.getTotalQuantiteDemandeeNonVendue()).add(qte)
            );
        }

        resume.setDerniereDemande(LocalDateTime.now());

        requisitionRepository.save(resume);
    }

    public List<RequisitionHistorique> getHistorique(LocalDate dateFrom, LocalDate dateTo) {
        return historiqueRepository.findByDateBetweenOrderByDateHeureDesc(dateFrom, dateTo);
    }

    public List<RequisitionResponse> getAll() {
        return requisitionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private RequisitionResponse mapToResponse(RequisitionProduit r) {
        int demandes = nvlInt(r.getTotalDemandes());
        int ventes = nvlInt(r.getTotalVentes());
        int manques = nvlInt(r.getTotalVentesManquees());

        BigDecimal tauxSatisfaction = BigDecimal.ZERO;
        BigDecimal tauxManque = BigDecimal.ZERO;

        if (demandes > 0) {
            tauxSatisfaction = BigDecimal.valueOf(ventes)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(demandes), 2, RoundingMode.HALF_UP);

            tauxManque = BigDecimal.valueOf(manques)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(demandes), 2, RoundingMode.HALF_UP);
        }

        Produit p = r.getProduit();

        return RequisitionResponse.builder()
                .id(r.getId())

                .produitId(p != null ? p.getId() : null)
                .produitNom(p != null ? p.getNom() : null)
                .codeBarres(p != null ? p.getCodeBarres() : null)
                .categorieNom(p != null && p.getCategorie() != null ? p.getCategorie().getNom() : null)

                .depotId(r.getDepot() != null ? r.getDepot().getId() : null)
                .depotNom(r.getDepot() != null ? r.getDepot().getNom() : null)

                .locatorId(r.getLocator() != null ? r.getLocator().getId() : null)

                .totalDemandes(demandes)
                .totalVentes(ventes)
                .totalVentesManquees(manques)

                .totalQuantiteVendue(nvl(r.getTotalQuantiteVendue()))
                .totalQuantiteDemandeeNonVendue(nvl(r.getTotalQuantiteDemandeeNonVendue()))

                .produitExistant(r.getProduitExistant())
                .origine(r.getOrigine())

                .tauxSatisfaction(tauxSatisfaction)
                .tauxManque(tauxManque)

                .derniereDemande(r.getDerniereDemande())
                .derniereDateDemande(
                        r.getDerniereDemande() != null ? r.getDerniereDemande().toLocalDate() : null
                )
                .build();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private int nvlInt(Integer value) {
        return value != null ? value : 0;
    }

}
