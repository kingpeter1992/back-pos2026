package com.king.pos.Web;


import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.king.pos.Dto.CreateCommandeAchatRequest;
import com.king.pos.Dto.Response.CommandeAchatResponse;
import com.king.pos.Dto.Response.CommandeDashboardResponse;
import com.king.pos.ImplementServices.CommandeAchatService;



@RestController
@RequestMapping("/api/achats/commandes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommandeAchatController {

    private final CommandeAchatService commandeAchatService;

    @PostMapping
    public ResponseEntity<CommandeAchatResponse> create(@RequestBody CreateCommandeAchatRequest request) {
        return ResponseEntity.ok(commandeAchatService.create(request));
    }

    @PutMapping("/{id}")
public ResponseEntity<CommandeAchatResponse> update(
        @PathVariable Long id,
        @RequestBody CreateCommandeAchatRequest request) {
    return ResponseEntity.ok(commandeAchatService.update(id, request));
}

    @PutMapping("/{id}/valider")
    public ResponseEntity<CommandeAchatResponse> valider(@PathVariable Long id) {
        return ResponseEntity.ok(commandeAchatService.valider(id));
    }

    
    @GetMapping
    public List<CommandeAchatResponse> findAll() {
        return commandeAchatService.findAll();
    }


        @GetMapping("/dashboard")
    public ResponseEntity<CommandeDashboardResponse> getDashboard() {
        return ResponseEntity.ok(commandeAchatService.getDashboard());
    }
}