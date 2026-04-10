package com.king.pos.ImplementServices;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.king.pos.Dao.ClientRepository;
import com.king.pos.Dao.TransactionCaisseRepository;
import com.king.pos.Dto.ClientDetailsDto;
import com.king.pos.Dto.ClientDto;
import com.king.pos.Dto.ClientStatsDto;
import com.king.pos.Dto.ReponseDto;

import com.king.pos.Dto.Response.TransactionMiniDto;
import com.king.pos.Entitys.Client;
import com.king.pos.Entitys.TransactionCaisse;
import com.king.pos.Entitys.TypeTransaction;
import com.king.pos.enums.Devise;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;
    private final TransactionCaisseRepository transactionRepository;

    // Création d'un client
    public Client creerClient(Client client) {
        client.setId(generateCode5());
        client.setNom(client.getNom().toUpperCase());
        client.setAdresse(client.getAdresse().toUpperCase());
        client.setContact(client.getContact().toUpperCase());
        client.setContact2(client.getContact2().toUpperCase());
        client.setEmail(client.getEmail().toUpperCase());
        client.setActif(true);

        return clientRepository.save(client);
    }

    private Long generateCode5() {
        int number = ThreadLocalRandom.current().nextInt(10000, 100000);
        return Long.valueOf(number);
    }

    // Signer un contrat pour un client
   

    // Historique des factures d'un client
   

    // Mettre à jour un client
    public ClientDto updateClient(Long id, Client client) {
        Client existing = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

        existing.setNom(client.getNom().toUpperCase());
        existing.setAdresse(client.getAdresse().toUpperCase());
        existing.setContact(client.getContact().toUpperCase());
        existing.setContact2(client.getContact2().toUpperCase());
        existing.setEmail(client.getEmail().toUpperCase());
        existing.setTypeClient(client.getTypeClient().toUpperCase());

        Client saved = clientRepository.save(existing);

        // Retourne le DTO pour éviter le lazy loading
        return new ClientDto(
                saved.getId(),
                saved.getNom(),
                saved.getAdresse(),
                saved.getContact(),
                saved.getContact2(),
                saved.getEmail(),
                saved.getTypeClient(),
                saved.isActif());
    }

    // Supprimer un client
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        clientRepository.delete(client);
    }

    // Liste de tous les clients
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    // Obtenir un client par id
    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
    }

    public ClientDetailsDto getClientDetails(Long clientId,
            LocalDate dateFrom,
            LocalDate dateTo

    ) {
        LocalDateTime from = dateFrom.atStartOfDay();
        LocalDateTime to = dateTo.atTime(23, 59, 59);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

        // Convertir Client → ClientDto
        ClientDto clientDto = mapToClientDto(client);

        // Récupérer les factures du client
      

        // le statitique
        ClientStatsDto getClientStats = getClientStats(clientId, dateFrom, dateTo);

      

        // trasactions

        List<TransactionMiniDto> tx = transactionRepository
                .findByClientIdAndDateTransactionBetween(clientId, from, to)
                .stream()
                .map(this::mapToClientToTrasaction)
                .toList();

      

        ClientDetailsDto result = new ClientDetailsDto();
        result.setClient(clientDto);
        result.setStats(getClientStats);
        result.setTransactions(tx);

        return result;

    }

    
        // ========================
    // MAPPING METHODS
    // ========================

    private ClientDto mapToClientDto(Client c) {
        ClientDto dto = new ClientDto();
        dto.setId(c.getId());
        dto.setNom(c.getNom().toUpperCase());
        dto.setContact(c.getContact());
        dto.setContact2(c.getContact2());
        dto.setEmail(c.getEmail());
        dto.setAdresse(c.getAdresse().toUpperCase());
        dto.setTypeClient(c.getTypeClient());
        return dto;
    }

    private TransactionMiniDto mapToClientToTrasaction(TransactionCaisse t) {
        TransactionMiniDto dto = new TransactionMiniDto();
        dto.setId(t.getId());
        dto.setMontant(t.getMontant());
        dto.setType(t.getType());
    //    dto.setCategory(t.getCategory());
        
        dto.setDevise(t.getDevise());
        dto.setReference(t.getReference());
        dto.setSoldeAvant(t.getSoldeAvant());
        dto.setSoldeApres(t.getSoldeApres());
        dto.setSens(t.getSens());
        dto.setUserId(t.getUserId());
        dto.setDateTransaction(t.getDateTransaction());
   //     dto.setCategory(t.getCategory());
        return dto;

    }

  
   

  
    public ClientStatsDto getClientStats(
            Long clientId,
            LocalDate dateFrom,
            LocalDate dateTo) {

        ClientStatsDto stats = new ClientStatsDto();

        // =========================
        // TRANSACTIONS (ENCAISSEMENTS)
        // =========================
        LocalDateTime from = dateFrom.atStartOfDay();
        LocalDateTime to = dateTo.atTime(23, 59, 59);
        List<TransactionCaisse> encaissements = transactionRepository
                .findByClientIdAndTypeAndDateTransactionBetween(
                        clientId,
                        TypeTransaction.ENCAISSEMENT,
                        from,
                        to);

        stats.setTotalTransactions(encaissements.size());

       double encCDF = 0.0;
double encUSD = 0.0;

for (TransactionCaisse t : encaissements) {

    double montant = t.getMontant() != 0 ? t.getMontant() : 0.0;

    if (t.getDevise() == Devise.CDF) {
        encCDF += montant;
    } else if (t.getDevise() == Devise.USD) {
        encUSD += montant;
    }
}


// ✅ Sécurité : éviter valeurs négatives si nécessaire
encCDF = Math.max(0, encCDF);
encUSD = Math.max(0, encUSD);

stats.setEncaissementsCDF(encCDF);
stats.setEncaissementsUSD(encUSD);

return stats;
    }

}
