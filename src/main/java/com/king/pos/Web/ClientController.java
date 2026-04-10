package com.king.pos.Web;


import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.king.pos.Dto.ClientDetailsDto;
import com.king.pos.Dto.ClientDto;
import com.king.pos.Entitys.Client;
import com.king.pos.ImplementServices.ClientService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ClientController {

    private final ClientService clientService;


    // Créer un nouveau client
    @PostMapping("/create")
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        Client savedClient = clientService.creerClient(client);
        return ResponseEntity.ok(savedClient);
    }

    // Modifier un client existant
  @PutMapping("/update/{id}")
public ResponseEntity<ClientDto> updateClient(@PathVariable Long id, @RequestBody Client client) {
    ClientDto updatedClient = clientService.updateClient(id, client);
    return ResponseEntity.ok(updatedClient);
}
    // Supprimer un client
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.ok("Client supprimé avec succès");
    }

 @GetMapping("/all")
public ResponseEntity<List<ClientDto>> getAllClients() {
    List<Client> clients = clientService.getAllClients();
    List<ClientDto> dtos = clients.stream()
        .map(c -> new ClientDto(c.getId(), c.getNom(), c.getAdresse(),
                               c.getContact(), c.getContact2(), c.getEmail(),
                               c.getTypeClient(),c.isActif()))
        .collect(Collectors.toList());
    return ResponseEntity.ok(dtos);
}

    // Consulter un client par id
    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

@GetMapping("/{clientId}/details")
public ResponseEntity<ClientDetailsDto> details(@PathVariable Long clientId,
        @RequestParam LocalDate dateFrom,
        @RequestParam LocalDate dateTo
) {

    ClientDetailsDto dto = clientService.getClientDetails(clientId,
        dateFrom,dateTo
    );

    return ResponseEntity.ok(dto);
}
}
