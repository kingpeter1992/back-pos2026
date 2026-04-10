package com.king.pos.Web;

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
import com.king.pos.Dto.OuvrirCaisseDTO;
import com.king.pos.Dto.TransactionCaisseDto;
import com.king.pos.Dto.Response.CaisseSessionResponseDTO;
import com.king.pos.Entitys.CaisseSession;
import com.king.pos.ImplementServices.CaisseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/caisse")
@RequiredArgsConstructor
public class CaisseController {

    private final CaisseService caisseService;


    @PostMapping("/ouvrir")
    public ResponseEntity<?> ouvrir(@RequestBody OuvrirCaisseDTO dto, Principal principal) {
        String username = principal.getName();
        return ResponseEntity.ok(caisseService.ouvrirCaisse(dto, username));
    }

    @PostMapping("/cloturer")
    public ResponseEntity<?> cloturer(@RequestBody CloturerCaisseDTO dto,
        Principal principal
    ) {
                String username = principal.getName();
             CaisseSession saved =   caisseService.cloturerCaisse(dto, username);

        return ResponseEntity.ok(toResponseCessionCaisse(saved));
    }


    private CaisseSessionResponseDTO toResponseCessionCaisse(CaisseSession session) {

    return CaisseSessionResponseDTO.builder()
            .id(session.getId())
            .dateJour(session.getDateJour())
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


    @GetMapping("/historique")
    public ResponseEntity<?> historique() {
        return ResponseEntity.ok(caisseService.historiqueDuJour());
    }

    @GetMapping("/session/ouverte")
public ResponseEntity<CaisseSessionDto> sessionOuverte() {
    return ResponseEntity.ok(caisseService.getSessionOuverteDuJour());
}

@GetMapping("/historique/jour")
public ResponseEntity<List<TransactionCaisseDto>> historiqueDuJour() {
    return ResponseEntity.ok(caisseService.historiqueDuJour());
}

@GetMapping("/report")
public ResponseEntity<?> rapportCaisse(
        @RequestParam LocalDate dateFrom,
        @RequestParam LocalDate dateTo
) {

    CaisseSummaryDTO summary = caisseService.buildSummary(dateFrom, dateTo);



    Map<String, Object> response = new HashMap<>();
    response.put("summary", summary);    

    return ResponseEntity.ok(response);
}







}
