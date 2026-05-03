package com.king.pos.Web;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.king.pos.Dto.CaisseSessionDto;
import com.king.pos.Dto.CaisseSummaryDTO;
import com.king.pos.Dto.CloturerCaisseDTO;
import com.king.pos.Dto.OperationCaisseDTO;
import com.king.pos.Dto.OuvrirCaisseDTO;
import com.king.pos.Dto.TransactionCaisseDto;
import com.king.pos.Dto.Response.CaisseSessionResponseDTO;
import com.king.pos.Entitys.CaisseSession;
import com.king.pos.ImplementServices.CaisseService;
import com.king.pos.ImplementServices.TauxChangeService;
import com.king.pos.request.TauxChangeRequest;
import com.king.pos.request.TauxChangeResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/caisse")
@RequiredArgsConstructor
public class CaisseController {

    private final CaisseService caisseService;
     private final TauxChangeService service;


@PostMapping("/ouvrir")
    public ResponseEntity<CaisseSessionDto> ouvrir(
            @RequestBody OuvrirCaisseDTO dto,
            Principal principal
    ) {
        String username = principal.getName();
        return ResponseEntity.ok(caisseService.ouvrirCaisse(dto, username));
    }

    @PostMapping("/cloturer")
    public ResponseEntity<CaisseSessionDto> cloturer(
            @RequestBody CloturerCaisseDTO dto,
            Principal principal
    ) {
        String username = principal.getName();
        return ResponseEntity.ok(caisseService.cloturerCaisse(dto, username));
    }

    @GetMapping("/session/ouverte")
    public ResponseEntity<CaisseSessionDto> sessionOuverte() {
        return ResponseEntity.ok(caisseService.getSessionOuverteDuJour());
    }

    @PostMapping("/operation")
    public ResponseEntity<TransactionCaisseDto> operation(
            @RequestBody OperationCaisseDTO dto,
            Principal principal
    ) {
        String username = principal.getName();
        return ResponseEntity.ok(caisseService.enregistrerOperation(dto, username));
    }

    @GetMapping("/historique/jour")
    public ResponseEntity<List<TransactionCaisseDto>> historiqueDuJour() {
        return ResponseEntity.ok(caisseService.historiqueDuJour());
    }

    @GetMapping("/report")
    public ResponseEntity<Map<String, Object>> rapportCaisse(
            @RequestParam LocalDate dateFrom,
            @RequestParam LocalDate dateTo
    ) {
        return ResponseEntity.ok(caisseService.buildReport(dateFrom, dateTo));
    }

    @GetMapping("/taux")
    public List<TauxChangeResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/taux/actif")
    public TauxChangeResponse getActif() {
        return service.getActif();
    }

    @GetMapping("/taux/last")
    public BigDecimal getValeurTauxActif() {
        return service.getValeurTauxActif();
    }

    @PostMapping("/taux")
    public TauxChangeResponse create(@Valid @RequestBody TauxChangeRequest request) {
        return service.create(request);
    }

    @PutMapping("/taux/{id}")
    public TauxChangeResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TauxChangeRequest request
    ) {
        return service.update(id, request);
    }
    
    @PatchMapping("/taux/{id}/activer")
    public TauxChangeResponse activer(@PathVariable Long id) {
        return service.activer(id);
    }

    @DeleteMapping("/taux/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

}
