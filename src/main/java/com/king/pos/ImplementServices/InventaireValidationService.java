package com.king.pos.ImplementServices;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.king.pos.Dao.InventaireBordereauRepository;
import com.king.pos.Dao.InventaireRepository;
import com.king.pos.Entitys.Inventaire;
import com.king.pos.Entitys.InventaireBordereau;
import com.king.pos.Handllers.BusinessException;
import com.king.pos.enums.StatutBordereauInventaire;
import com.king.pos.enums.StatutInventaire;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventaireValidationService {

    private final InventaireRepository inventaireRepository;
    private final InventaireBordereauRepository bordereauRepository;
    private final StockAjustementInventaireService stockAjustementInventaireService;

    @Transactional
    public void validerInventaire(Long inventaireId, String user) {
        Inventaire inventaire = inventaireRepository.findById(inventaireId)
                .orElseThrow(() -> new BusinessException("Inventaire introuvable"));

        if (inventaire.getStatut() == StatutInventaire.CLOTURE) {
            throw new BusinessException("Inventaire déjà clôturé.");
        }

        if (inventaire.getStatut() == StatutInventaire.ANNULE) {
            throw new BusinessException("Inventaire annulé.");
        }

        if (inventaire.getStatut() != StatutInventaire.VARIANCE_LANCEE) {
            throw new BusinessException("Veuillez lancer les variances avant de valider l'inventaire.");
        }

        inventaire.setValide(true);
        inventaire.setValidePar(user);
        inventaire.setDateValidation(LocalDateTime.now());
        inventaire.setStatut(StatutInventaire.VALIDE);
    }

  @Transactional
public void cloturerInventaire(Long inventaireId, String user) {
    Inventaire inventaire = inventaireRepository.findById(inventaireId)
            .orElseThrow(() -> new BusinessException("Inventaire introuvable"));

    if (Boolean.TRUE.equals(inventaire.getCloture())
            || inventaire.getStatut() == StatutInventaire.CLOTURE) {
        throw new BusinessException("Cet inventaire est déjà clôturé.");
    }

    boolean existeBordereauNonMisAJour =
            bordereauRepository.existsByInventaireIdAndStatutNot(
                    inventaireId,
                    StatutBordereauInventaire.STOCK_MIS_A_JOUR
            );

    if (existeBordereauNonMisAJour) {
        throw new BusinessException(
                "Impossible de clôturer : tous les bordereaux ne sont pas encore mis à jour en stock."
        );
    }

    inventaire.setCloture(true);
    inventaire.setCloturePar(user);
    inventaire.setDateCloture(LocalDateTime.now());
    inventaire.setStatut(StatutInventaire.CLOTURE);

    inventaireRepository.save(inventaire);
}

    @Transactional
    public void annulerInventaire(Long inventaireId, String user, String commentaire) {
        Inventaire inventaire = inventaireRepository.findById(inventaireId)
                .orElseThrow(() -> new BusinessException("Inventaire introuvable"));

        if (inventaire.getStatut() == StatutInventaire.CLOTURE) {
            throw new BusinessException("Impossible d'annuler un inventaire clôturé.");
        }

        if (inventaire.getStatut() == StatutInventaire.ANNULE) {
            throw new BusinessException("Inventaire déjà annulé.");
        }

        List<InventaireBordereau> bordereaux = bordereauRepository.findByInventaireId(inventaireId);

        for (InventaireBordereau bordereau : bordereaux) {
            if (Boolean.TRUE.equals(bordereau.getStockMisAJour())) {
                stockAjustementInventaireService.annulerAjustementsDepuisReference(bordereau.getReference());
                bordereau.setStockMisAJour(false);
                bordereau.setStatut(StatutBordereauInventaire.ANNULE);
            } else {
                bordereau.setStatut(StatutBordereauInventaire.ANNULE);
            }
        }

        inventaire.setAnnule(true);
        inventaire.setAnnulePar(user);
        inventaire.setDateAnnulation(LocalDateTime.now());
        inventaire.setCommentaireAnnulation(commentaire);
        inventaire.setStatut(StatutInventaire.ANNULE);
    }

}