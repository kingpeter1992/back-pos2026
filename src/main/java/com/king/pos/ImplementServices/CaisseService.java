package com.king.pos.ImplementServices;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


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
    private final ClientRepository clientRepository;
    private final CaisseSessionRepository caisseSessionRepository;



    @Transactional
    public CaisseSession ouvrirCaisse(OuvrirCaisseDTO dto, String userId) {

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

        TauxJournalier tx = new TauxJournalier();

        tx.setDate(LocalDate.now());
        tx.setTaux(dto.getTauxChange());

        return caisseSessionRepository.save(s);
    }

    @Transactional
    public CaisseSession cloturerCaisse(CloturerCaisseDTO dto, String userId) {

        CaisseSession s = caisseSessionRepository
                .findByDateJourAndStatut(LocalDate.now(), "OUVERTE")
                .orElseThrow(() -> new RuntimeException("Aucune caisse ouverte aujourd'hui"));

        s.setStatut("FERMEE");
        s.setDateCloture(LocalDateTime.now());
        s.setClosedBy(userId);
        s.setNoteCloture(dto.getNote());

        return caisseSessionRepository.save(s);
    }

   

    private double convertir(double montant, Devise from, Devise to, double taux) {

        if (from == null || to == null) {
            throw new RuntimeException("Devise null");
        }
        if (taux <= 0) {
            throw new RuntimeException("Taux invalide");
        }

        if (from == to)
            return montant;

        // USD -> CDF
        if (from == Devise.USD && to == Devise.CDF) {
            return montant * taux;
        }

        // CDF -> USD
        if (from == Devise.CDF && to == Devise.USD) {
            return montant / taux;
        }

        throw new RuntimeException("Devise non supportée: " + from + " -> " + to);
    }

    public CaisseSessionDto getSessionOuverteDuJour() {

        CaisseSession session = caisseSessionRepository
                .findByDateJourAndStatut(LocalDate.now(),  "OUVERTE")
                .orElseThrow(() -> new RuntimeException("Aucune caisse ouverte aujourd'hui"));

        return CaisseSessionDto.builder()
                .id(session.getId())
                .dateJour(session.getDateJour())
                .statut(session.getStatut())
                .dateOuverture(session.getDateOuverture())
                .dateCloture(session.getDateCloture())
                .soldeInitialUSD(session.getSoldeInitialUSD())
                .soldeInitialCDF(session.getSoldeInitialCDF())
                .soldeActuelUSD(session.getSoldeActuelUSD())
                .soldeActuelCDF(session.getSoldeActuelCDF())
                .openedBy(session.getOpenedBy())
                .closedBy(session.getClosedBy())
                .noteOuverture(session.getNoteOuverture())
                .noteCloture(session.getNoteCloture())
                .build();
    }

    // @Transactional(readOnly = true)
    public List<TransactionCaisseDto> historiqueDuJour() {

        CaisseSession session = caisseSessionRepository
                .findByDateJourAndStatut(LocalDate.now(), "OUVERTE")
                .orElseThrow(() -> new RuntimeException("Aucune caisse ouverte aujourd'hui"));

        return null;
    }

    public CaisseSummaryDTO buildSummary(LocalDate from, LocalDate to) {

        List<TransactionCaisse> txs = transactionRepository.findByDateRange(
                from.atStartOfDay(),
                to.plusDays(1).atStartOfDay());

        double encUSD = 0, decUSD = 0, encCDF = 0, decCDF = 0;

        for (TransactionCaisse t : txs) {

            if (t.getDevise() == Devise.USD) {
                if (t.getType() == TypeTransaction.ENCAISSEMENT)
                    encUSD += t.getMontant();
                else
                    decUSD += t.getMontant();
            }

            if (t.getDevise() == Devise.CDF) {
                if (t.getType() == TypeTransaction.ENCAISSEMENT)
                    encCDF += t.getMontant();
                else
                    decCDF += t.getMontant();
            }
        }

        return new CaisseSummaryDTO(encUSD, decUSD, encCDF, decCDF);
    }

    

    @Transactional
    public TransactionCaisse encaisserClientPro(OperationCaisseDTO dto, String username) {

        // =========================
        // 0) VALIDATIONS
        // =========================
        if (dto == null)
            throw new RuntimeException("Payload vide");
        if (dto.getType() == null)
            throw new RuntimeException("Type transaction obligatoire");
        if (dto.getDevise() == null)
            throw new RuntimeException("Devise obligatoire");
        if (dto.getModePaiement() == null)
            throw new RuntimeException("Mode de paiement obligatoire");
        if (dto.getMontant() <= 0)
            throw new RuntimeException("Montant invalide");

        // ✅ category = classification (pas de logique métier)
        if (dto.getCategory() == null) {
            throw new RuntimeException("Catégorie obligatoire"); // ou tu mets une valeur par défaut
        }

        // =========================
        // 1) SESSION OUVERTE + LOCK
        // =========================
        CaisseSession session = caisseSessionRepository
                .findByDateJourAndStatut(LocalDate.now(), "OUVERTE")
                .orElseThrow(() -> new RuntimeException("Caisse fermée. Ouvre la caisse d'abord."));

        session = caisseSessionRepository.lockById(session.getId())
                .orElseThrow(() -> new RuntimeException("Session caisse introuvable"));

        // =========================
        // 2) LIEN : client OU gardien OU personne
        // =========================
        final Long clientId = (dto.getClientId() != null && dto.getClientId() > 0) ? dto.getClientId() : null;

        Client client = null;
        if (clientId != null) {
            client = clientRepository.findByIdNative(dto.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client introuvable: " + clientId));
        }

        // =========================
        // 3) SOLDE AVANT / INSUFFISANCE
        // =========================
        double soldeAvant = (dto.getDevise() == Devise.USD)
                ? session.getSoldeActuelUSD()
                : session.getSoldeActuelCDF();

        final double EPS = 0.0001;
        if (dto.getType() == TypeTransaction.DECAISSEMENT && (soldeAvant + EPS) < dto.getMontant()) {
            throw new RuntimeException("Solde insuffisant en " + dto.getDevise()
                    + " (Disponible: " + soldeAvant + ")");
        }

        // =========================
        // 4) SOLDE APRES + MAJ SESSION
        // =========================
        double soldeApres = (dto.getType() == TypeTransaction.ENCAISSEMENT)
                ? soldeAvant + dto.getMontant()
                : soldeAvant - dto.getMontant();

        soldeApres = Math.round(soldeApres * 100.0) / 100.0;
        if (dto.getDevise() == Devise.USD)
            session.setSoldeActuelUSD(soldeApres);
        else
            session.setSoldeActuelCDF(soldeApres);
        caisseSessionRepository.save(session);

        // =========================
        // 5) CREER TRANSACTION
        // =========================
        TransactionCaisse tx = new TransactionCaisse();
        tx.setSession(session);
        tx.setDateTransaction(LocalDateTime.now());
        tx.setUserId(username);

        tx.setType(dto.getType());
        tx.setDevise(dto.getDevise());
        tx.setMontant(dto.getMontant());
        tx.setModePaiement(dto.getModePaiement());

        // ✅ category uniquement pour classification
   //     tx.setCategory(dto.getCategory());

        tx.setDescription(dto.getDescription() == null ? "" : dto.getDescription());

        String ref = dto.getReference();
        if (ref == null || ref.isBlank())
            ref = "TX-" + System.currentTimeMillis();
        tx.setReference(ref);

        tx.setSoldeAvant(soldeAvant);
        tx.setSoldeApres(soldeApres);
        tx.setSens(dto.getType() == TypeTransaction.ENCAISSEMENT ? "+" : "-");

        if (client != null)
            tx.setClient(client);

        TransactionCaisse saved = transactionRepository.save(tx);

        
        return saved;
    }

    @Transactional
    public TransactionCaisse enregistrerOperationGardien(OperationCaisseDTO dto, String username) {

        // 0) validations communes
        if (dto == null)
            throw new RuntimeException("Payload vide");
        if (dto.getType() == null)
            throw new RuntimeException("Type transaction obligatoire");
        if (dto.getDevise() == null)
            throw new RuntimeException("Devise obligatoire");
        if (dto.getCategory() == null)
            throw new RuntimeException("Catégorie obligatoire");
        if (dto.getModePaiement() == null)
            throw new RuntimeException("Mode de paiement obligatoire");
        if (dto.getMontant() <= 0)
            throw new RuntimeException("Montant invalide");

        // 1) validations gardien
        final Long gardienId = (dto.getGardienId() != null && dto.getGardienId() > 0) ? dto.getGardienId() : null;
        if (gardienId == null)
            throw new RuntimeException("Gardien obligatoire");

      
        // 2) session + lock
        CaisseSession session = caisseSessionRepository
                .findByDateJourAndStatut(LocalDate.now(), "OUVERTE")
                .orElseThrow(() -> new RuntimeException("Caisse fermée. Ouvre la caisse d'abord."));
        session = caisseSessionRepository.lockById(session.getId())
                .orElseThrow(() -> new RuntimeException("Session caisse introuvable"));

       
        // 4) solde avant + insuffisance
        double soldeAvant = (dto.getDevise() == Devise.USD)
                ? session.getSoldeActuelUSD()
                : session.getSoldeActuelCDF();

        final double EPS = 0.0001;
        if (dto.getType() == TypeTransaction.DECAISSEMENT && (soldeAvant + EPS) < dto.getMontant()) {
            throw new RuntimeException("Solde insuffisant en " + dto.getDevise() + " (Disponible: " + soldeAvant + ")");
        }

        // 5) solde après + maj session
        double soldeApres = (dto.getType() == TypeTransaction.ENCAISSEMENT)
                ? soldeAvant + dto.getMontant()
                : soldeAvant - dto.getMontant();

        soldeApres = Math.round(soldeApres * 100.0) / 100.0;

        if (dto.getDevise() == Devise.USD)
            session.setSoldeActuelUSD(soldeApres);
        else
            session.setSoldeActuelCDF(soldeApres);

        caisseSessionRepository.save(session);

        // 6) tx
        TransactionCaisse tx = new TransactionCaisse();
        tx.setSession(session);
        tx.setDateTransaction(LocalDateTime.now());
        tx.setUserId(username);

        tx.setType(dto.getType());
  //     tx.setCategory(dto.getCategory());
        tx.setDevise(dto.getDevise());
        tx.setMontant(dto.getMontant());
        tx.setModePaiement(dto.getModePaiement());
        tx.setDescription(dto.getDescription() == null ? "" : dto.getDescription());

        String ref = dto.getReference();
      
        tx.setReference(ref);

        tx.setSoldeAvant(soldeAvant);
        tx.setSoldeApres(soldeApres);
        tx.setSens(dto.getType() == TypeTransaction.ENCAISSEMENT ? "+" : "-");


        TransactionCaisse saved = transactionRepository.save(tx);

       

        return saved;
    }

    @Transactional
    public TransactionCaisse enregistrerOperationAutre(OperationCaisseDTO dto, String username) {

        // 0) validations communes
        if (dto == null)
            throw new RuntimeException("Payload vide");
        if (dto.getType() == null)
            throw new RuntimeException("Type transaction obligatoire");
        if (dto.getDevise() == null)
            throw new RuntimeException("Devise obligatoire");
        if (dto.getCategory() == null)
            throw new RuntimeException("Catégorie obligatoire");
        if (dto.getModePaiement() == null)
            throw new RuntimeException("Mode de paiement obligatoire");
        if (dto.getMontant() <= 0)
            throw new RuntimeException("Montant invalide");

        // 1) session + lock
        CaisseSession session = caisseSessionRepository
                .findByDateJourAndStatut(LocalDate.now(),"OUVERTE")
                .orElseThrow(() -> new RuntimeException("Caisse fermée. Ouvre la caisse d'abord."));
        session = caisseSessionRepository.lockById(session.getId())
                .orElseThrow(() -> new RuntimeException("Session caisse introuvable"));

        // 2) ids optionnels
        final Long clientId = (dto.getClientId() != null && dto.getClientId() > 0) ? dto.getClientId() : null;
        final Long gardienId = (dto.getGardienId() != null && dto.getGardienId() > 0) ? dto.getGardienId() : null;

        if (clientId != null && gardienId != null) {
            throw new RuntimeException("Transaction invalide : client et gardien à la fois");
        }

        Client client = null;

        if (clientId != null) {
            client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client introuvable: " + clientId));
        }
     

        // 3) solde avant + insuffisance
        double soldeAvant = (dto.getDevise() == Devise.USD)
                ? session.getSoldeActuelUSD()
                : session.getSoldeActuelCDF();

        final double EPS = 0.0001;
        if (dto.getType() == TypeTransaction.DECAISSEMENT && (soldeAvant + EPS) < dto.getMontant()) {
            throw new RuntimeException("Solde insuffisant en " + dto.getDevise() + " (Disponible: " + soldeAvant + ")");
        }

        // 4) solde après + maj session
        double soldeApres = (dto.getType() == TypeTransaction.ENCAISSEMENT)
                ? soldeAvant + dto.getMontant()
                : soldeAvant - dto.getMontant();

        soldeApres = Math.round(soldeApres * 100.0) / 100.0;

        if (dto.getDevise() == Devise.USD)
            session.setSoldeActuelUSD(soldeApres);
        else
            session.setSoldeActuelCDF(soldeApres);

        caisseSessionRepository.save(session);

        // 5) tx
        TransactionCaisse tx = new TransactionCaisse();
        tx.setSession(session);
        tx.setDateTransaction(LocalDateTime.now());
        tx.setUserId(username);

        tx.setType(dto.getType());
     //   tx.setCategory(dto.getCategory());
        tx.setDevise(dto.getDevise());
        tx.setMontant(dto.getMontant());
        tx.setModePaiement(dto.getModePaiement());
        tx.setDescription(dto.getDescription() == null ? "" : dto.getDescription());

        String ref = dto.getReference();
    
        tx.setReference(ref);

        tx.setSoldeAvant(soldeAvant);
        tx.setSoldeApres(soldeApres);
        tx.setSens(dto.getType() == TypeTransaction.ENCAISSEMENT ? "+" : "-");

        if (client != null)
            tx.setClient(client);
      

        TransactionCaisse saved = transactionRepository.save(tx);

        // 6) historique si client ou gardien
     
        return saved;
    }



}