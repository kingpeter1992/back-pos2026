package com.king.pos.Web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.king.pos.Dto.CreateReceptionAchatRequest;
import com.king.pos.Dto.Response.DepotResponse;
import com.king.pos.Dto.Response.ReceptionAchatResponse;
import com.king.pos.ImplementServices.ReceptionAchatCreationService;
import com.king.pos.Interface.DepotService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/achats/receptions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReceptionAchatController {

    private final ReceptionAchatCreationService receptionAchatCreationService;
    private final DepotService depotService;

    @PostMapping
    public ResponseEntity<ReceptionAchatResponse> create(@RequestBody CreateReceptionAchatRequest request) {
        return ResponseEntity.ok(receptionAchatCreationService.create(request));
    }


    @GetMapping("depots")
    public ResponseEntity<List<DepotResponse>> getAllDepot() {
        return ResponseEntity.ok(depotService.getAll());
    }

    @GetMapping("depots/{id}")
    public ResponseEntity<DepotResponse> getByIdDepot(@PathVariable Long id) {
        return ResponseEntity.ok(depotService.getById(id));
    }

     @GetMapping
    public List<ReceptionAchatResponse> getAll() {
        return receptionAchatCreationService.getAll();
    }

    @GetMapping("/{id}")
    public ReceptionAchatResponse getById(@PathVariable Long id) {
        return receptionAchatCreationService.getById(id);
    }

    @GetMapping("/commande/{commandeId}")
    public List<ReceptionAchatResponse> findByCommande(@PathVariable Long commandeId) {
        return receptionAchatCreationService.findByCommande(commandeId);
    }
 
}
