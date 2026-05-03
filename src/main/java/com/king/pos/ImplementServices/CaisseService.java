package com.king.pos.ImplementServices;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.king.pos.Dao.CaisseSessionRepository;
import com.king.pos.Dao.ClientRepository;
import com.king.pos.Dao.TransactionCaisseRepository;
import com.king.pos.Dto.CaisseSessionDto;
import com.king.pos.Dto.CaisseSummaryDTO;
import com.king.pos.Dto.CloturerCaisseDTO;
import com.king.pos.Dto.OperationCaisseDTO;
import com.king.pos.Dto.OuvrirCaisseDTO;
import com.king.pos.Dto.TransactionCaisseDto;
import com.king.pos.Entitys.CaisseSession;
import com.king.pos.Entitys.Client;
import com.king.pos.Entitys.TauxJournalier;
import com.king.pos.Entitys.TransactionCaisse;
import com.king.pos.Entitys.TypeTransaction;
import com.king.pos.enums.Devise;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CaisseService {

    private final TransactionCaisseRepository transactionRepository;
    private final CaisseSessionRepository caisseSessionRepository;
    private final TauxChangeService tauxChangeService;

    public CaisseSessionDto ouvrirCaisse(OuvrirCaisseDTO dto, String userId) {

        caisseSessionRepository.findByDateJourAndStatut(LocalDate.now(), "OUVERTE")
                .ifPresent(s -> {
                    throw new RuntimeException("Une caisse est déjà ouverte aujourd'hui");
                });

        CaisseSession s = new CaisseSession();
        s.setDateJour(LocalDate.now());
        s.setStatut("OUVERTE");
        s.setDateOuverture(LocalDateTime.now());
        s.setOpenedBy(userId);

        s.setSoldeInitialUSD(dto.getSoldeInitialUSD());
        s.setSoldeInitialCDF(dto.getSoldeInitialCDF());

        s.setSoldeActuelUSD(dto.getSoldeInitialUSD());
        s.setSoldeActuelCDF(dto.getSoldeInitialCDF());

        s.setNoteOuverture(dto.getNote());

        CaisseSession saved = caisseSessionRepository.save(s);

        return toSessionDto(saved);
    }

    public CaisseSessionDto cloturerCaisse(CloturerCaisseDTO dto, String userId) {

        CaisseSession s = caisseSessionRepository
                .findByDateJourAndStatut(LocalDate.now(), "OUVERTE")
                .orElseThrow(() -> new RuntimeException("Aucune caisse ouverte aujourd'hui"));

        s.setStatut("FERMEE");
        s.setDateCloture(LocalDateTime.now());
        s.setClosedBy(userId);
        s.setNoteCloture(dto.getNote());

        CaisseSession saved = caisseSessionRepository.save(s);

        return toSessionDto(saved);
    }

    public CaisseSessionDto getSessionOuverteDuJour() {

        CaisseSession session = caisseSessionRepository
                .findByDateJourAndStatut(LocalDate.now(), "OUVERTE")
                .orElseThrow(() -> new RuntimeException("Aucune caisse ouverte aujourd'hui"));

        return toSessionDto(session);
    }

    public List<TransactionCaisseDto> historiqueDuJour() {

        CaisseSession session = caisseSessionRepository
                .findByDateJourAndStatut(LocalDate.now(), "OUVERTE")
                .orElseThrow(() -> new RuntimeException("Aucune caisse ouverte aujourd'hui"));

        return transactionRepository.findBySessionIdOrderByDateTransactionDesc(session.getId())
                .stream()
                .map(this::toTransactionDto)
                .toList();
    }

    public TransactionCaisseDto enregistrerOperation(OperationCaisseDTO dto, String username) {

        validateOperation(dto);

        CaisseSession session = caisseSessionRepository
                .findByDateJourAndStatut(LocalDate.now(), "OUVERTE")
                .orElseThrow(() -> new RuntimeException("Caisse fermée. Ouvre la caisse d'abord."));

        session = caisseSessionRepository.lockById(session.getId())
                .orElseThrow(() -> new RuntimeException("Session caisse introuvable"));

        double taux = resolveTaux(dto);

        double soldeAvant = dto.getDevise() == Devise.USD
                ? session.getSoldeActuelUSD()
                : session.getSoldeActuelCDF();

        if (dto.getType() == TypeTransaction.DECAISSEMENT && soldeAvant < dto.getMontant()) {
            throw new RuntimeException("Solde insuffisant en " + dto.getDevise()
                    + " disponible : " + soldeAvant);
        }

        double soldeApres = dto.getType() == TypeTransaction.ENCAISSEMENT
                ? soldeAvant + dto.getMontant()
                : soldeAvant - dto.getMontant();

        soldeApres = round2(soldeApres);

        if (dto.getDevise() == Devise.USD) {
            session.setSoldeActuelUSD(soldeApres);
        } else {
            session.setSoldeActuelCDF(soldeApres);
        }

        caisseSessionRepository.save(session);

        double montantUSD = dto.getDevise() == Devise.USD
                ? dto.getMontant()
                : convertir(dto.getMontant(), Devise.CDF, Devise.USD, taux);

        double montantCDF = dto.getDevise() == Devise.CDF
                ? dto.getMontant()
                : convertir(dto.getMontant(), Devise.USD, Devise.CDF, taux);

        TransactionCaisse tx = new TransactionCaisse();
        tx.setSession(session);
        tx.setDateTransaction(LocalDateTime.now());
        tx.setUserId(username);

        tx.setType(dto.getType());
        tx.setDevise(dto.getDevise());
        tx.setMontant(round2(dto.getMontant()));
        tx.setCategory(dto.getCategory());
        tx.setModePaiement(dto.getModePaiement());

        tx.setDescription(dto.getDescription() == null ? "" : dto.getDescription().trim());

        String ref = dto.getReference();
        if (ref == null || ref.isBlank()) {
            ref = dto.getCategory() + "-" + System.currentTimeMillis();
        }

        tx.setReference(ref);
        tx.setSoldeAvant(round2(soldeAvant));
        tx.setSoldeApres(round2(soldeApres));
        tx.setSens(dto.getType() == TypeTransaction.ENCAISSEMENT ? "+" : "-");

        tx.setTauxChange(taux);
        tx.setMontantConvertiUSD(round2(montantUSD));
        tx.setMontantConvertiCDF(round2(montantCDF));

        TransactionCaisse saved = transactionRepository.save(tx);

        return toTransactionDto(saved);
    }

    public Map<String, Object> buildReport(LocalDate from, LocalDate to) {

        List<TransactionCaisse> txs = transactionRepository.findByDateRange(
                from.atStartOfDay(),
                to.plusDays(1).atStartOfDay()
        );

        CaisseSummaryDTO summary = buildSummaryFromTransactions(txs);

        Map<String, Object> response = new HashMap<>();
        response.put("summary", summary);
        response.put("operations", txs.stream().map(this::toTransactionDto).toList());

        return response;
    }

    private CaisseSummaryDTO buildSummaryFromTransactions(List<TransactionCaisse> txs) {

        double encUSD = 0;
        double decUSD = 0;
        double encCDF = 0;
        double decCDF = 0;

        for (TransactionCaisse t : txs) {
            if (t.getDevise() == Devise.USD) {
                if (t.getType() == TypeTransaction.ENCAISSEMENT) {
                    encUSD += t.getMontant();
                } else {
                    decUSD += t.getMontant();
                }
            }

            if (t.getDevise() == Devise.CDF) {
                if (t.getType() == TypeTransaction.ENCAISSEMENT) {
                    encCDF += t.getMontant();
                } else {
                    decCDF += t.getMontant();
                }
            }
        }

        return new CaisseSummaryDTO(
                round2(encUSD),
                round2(decUSD),
                round2(encCDF),
                round2(decCDF)
        );
    }

    private void validateOperation(OperationCaisseDTO dto) {
        if (dto == null) {
            throw new RuntimeException("Payload vide");
        }

        if (dto.getType() == null) {
            throw new RuntimeException("Type transaction obligatoire");
        }

        if (dto.getDevise() == null) {
            throw new RuntimeException("Devise obligatoire");
        }

        if (dto.getMontant() <= 0) {
            throw new RuntimeException("Montant invalide");
        }

        if (dto.getCategory() == null || dto.getCategory().isBlank()) {
            throw new RuntimeException("Catégorie obligatoire");
        }

        if (dto.getModePaiement() == null) {
            throw new RuntimeException("Mode de paiement obligatoire");
        }
    }

    private double resolveTaux(OperationCaisseDTO dto) {
        if (dto.getTauxChange() != null && dto.getTauxChange() > 0) {
            return dto.getTauxChange();
        }

        double tauxActif = tauxChangeService.getValeurTauxActif().doubleValue();

        if (tauxActif <= 0) {
            throw new RuntimeException("Aucun taux actif valide trouvé");
        }

        return tauxActif;
    }

    private double convertir(double montant, Devise from, Devise to, double taux) {
        if (from == to) {
            return montant;
        }

        if (taux <= 0) {
            throw new RuntimeException("Taux invalide");
        }

        if (from == Devise.USD && to == Devise.CDF) {
            return montant * taux;
        }

        if (from == Devise.CDF && to == Devise.USD) {
            return montant / taux;
        }

        throw new RuntimeException("Conversion non supportée");
    }

    private CaisseSessionDto toSessionDto(CaisseSession session) {
        return CaisseSessionDto.builder()
                .id(session.getId())
                .dateJour(session.getDateJour())
                .statut(session.getStatut())
                .soldeInitialUSD(session.getSoldeInitialUSD())
                .soldeInitialCDF(session.getSoldeInitialCDF())
                .soldeActuelUSD(session.getSoldeActuelUSD())
                .soldeActuelCDF(session.getSoldeActuelCDF())
                .dateOuverture(session.getDateOuverture())
                .dateCloture(session.getDateCloture())
                .openedBy(session.getOpenedBy())
                .closedBy(session.getClosedBy())
                .noteOuverture(session.getNoteOuverture())
                .noteCloture(session.getNoteCloture())
                .build();
    }

    private TransactionCaisseDto toTransactionDto(TransactionCaisse tx) {
        return TransactionCaisseDto.builder()
                .id(tx.getId())
                .dateTransaction(tx.getDateTransaction())
                .reference(tx.getReference())
                .type(tx.getType())
                .devise(tx.getDevise())
                .montant(tx.getMontant())
                .category(tx.getCategory())
                .modePaiement(tx.getModePaiement())
                .description(tx.getDescription())
                .soldeAvant(tx.getSoldeAvant())
                .soldeApres(tx.getSoldeApres())
                .sens(tx.getSens())
                .userId(tx.getUserId())
                .tauxChange(tx.getTauxChange())
                .montantConvertiUSD(tx.getMontantConvertiUSD())
                .montantConvertiCDF(tx.getMontantConvertiCDF())
                .build();
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}