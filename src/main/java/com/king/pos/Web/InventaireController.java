package com.king.pos.Web;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.king.pos.Dto.InventaireBordereauLigneUpdateRequest;
import com.king.pos.Dto.InventaireCreateRequest;
import com.king.pos.Dto.InventaireGenererBordereauxRequest;
import com.king.pos.Dto.Response.InventaireArticleResponse;
import com.king.pos.Dto.Response.InventaireBordereauLigneResponse;
import com.king.pos.Dto.Response.InventaireBordereauResponse;
import com.king.pos.Dto.Response.InventaireResponse;
import com.king.pos.Dto.Response.InventaireVarianceResponse;
import com.king.pos.Dto.Response.InventaireVarianceResumeResponse;
import com.king.pos.ImplementServices.InventaireService;
import com.king.pos.ImplementServices.InventaireValidationService;
import com.king.pos.ImplementServices.InventaireVarianceService;
import com.king.pos.ImplementServices.InventaireBordereauService;
import java.util.List;

@RestController
@RequestMapping("/api/inventaires")
@RequiredArgsConstructor
@CrossOrigin
public class InventaireController {

    private final InventaireService inventaireService;
    private final InventaireBordereauService bordereauService;
    private final InventaireVarianceService varianceService;
    private final InventaireValidationService validationService;

    @PostMapping
    public InventaireResponse create(@RequestBody InventaireCreateRequest request) {
        return inventaireService.create(request);
    }

    @GetMapping
    public List<InventaireResponse> getAll() {
        return inventaireService.getAll();
    }

    @GetMapping("/{id}")
    public InventaireResponse getById(@PathVariable Long id) {
        return inventaireService.getById(id);
    }

    @PostMapping("/{id}/ouvrir")
    public InventaireResponse ouvrir(@PathVariable Long id) {
        return inventaireService.ouvrir(id);
    }

    @GetMapping("/{inventaireId}/articles")
    public List<InventaireArticleResponse> getArticles(@PathVariable Long inventaireId) {
        return inventaireService.getArticlesByInventaire(inventaireId);
    }

    @PostMapping("/{inventaireId}/generer-bordereaux")
    public List<InventaireBordereauResponse> genererBordereaux(
            @PathVariable Long inventaireId,
            @RequestBody InventaireGenererBordereauxRequest request) {
        return bordereauService.genererBordereaux(inventaireId, request);
    }

    @GetMapping("/{inventaireId}/bordereaux")
    public List<InventaireBordereauResponse> getBordereaux(@PathVariable Long inventaireId) {
        return bordereauService.getBordereauxByInventaire(inventaireId);
    }

    @GetMapping("/bordereaux/{bordereauId}/lignes")
    public List<InventaireBordereauLigneResponse> getLignes(@PathVariable Long bordereauId) {
        return bordereauService.getLignesByBordereau(bordereauId);
    }

    @PutMapping("/bordereaux/{bordereauId}/lignes")
    public void saveLignes(
            @PathVariable Long bordereauId,
            @RequestBody List<InventaireBordereauLigneUpdateRequest> request) {
        bordereauService.saveLignes(bordereauId, request);
    }

    @PostMapping("/bordereaux/{bordereauId}/valider")
    public void validerBordereau(@PathVariable Long bordereauId, @RequestParam String user) {
        bordereauService.validerBordereau(bordereauId, user);
    }

    @PostMapping("/bordereaux/{bordereauId}/mise-a-jour-stock")
    public void miseAJourStock(@PathVariable Long bordereauId, @RequestParam String user) {
        bordereauService.miseAJourStock(bordereauId, user);
    }

    @PostMapping("/bordereaux/{bordereauId}/lancer-variances")
    public void lancerVariancesParBordereau(@PathVariable Long bordereauId) {
        varianceService.lancerVariancesParBordereau(bordereauId);
    }

    @GetMapping("/{inventaireId}/variances")
    public List<InventaireVarianceResponse> getVariances(@PathVariable Long inventaireId) {
        return varianceService.getVariances(inventaireId);
    }

    @GetMapping("/{inventaireId}/variances/resume")
    public ResponseEntity<InventaireVarianceResumeResponse> getResumeVariances(@PathVariable Long inventaireId) {
        return ResponseEntity.ok(varianceService.getResumeVariances(inventaireId));
    }

    @GetMapping("/variances")
    public ResponseEntity<List<InventaireVarianceResponse>> getAllVariances() {
        return ResponseEntity.ok(varianceService.getAllVariances());
    }

    @PostMapping("/{inventaireId}/valider")
    public void validerInventaire(@PathVariable Long inventaireId, @RequestParam String user) {
        validationService.validerInventaire(inventaireId, user);
    }

    @PostMapping("/{inventaireId}/cloturer")
    public void cloturerInventaire(@PathVariable Long inventaireId, @RequestParam String user) {
        validationService.cloturerInventaire(inventaireId, user);
    }

    @PostMapping("/{inventaireId}/annuler")
    public void annulerInventaire(
            @PathVariable Long inventaireId,
            @RequestParam String user,
            @RequestParam(required = false) String commentaire) {
        validationService.annulerInventaire(inventaireId, user, commentaire);
    }

}