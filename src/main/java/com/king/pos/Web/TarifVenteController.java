package com.king.pos.Web;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.king.pos.Dto.TarifCategorieProduitRequest;
import com.king.pos.Dto.TarifVenteRequest;
import com.king.pos.Dto.TarificationLotRequest;
import com.king.pos.Dto.TarificationProduitRequest;
import com.king.pos.Dto.Response.TarifCategorieProduitResponse;
import com.king.pos.Dto.Response.TarifVenteResponse;
import com.king.pos.Dto.Response.TarificationResponse;
import com.king.pos.ImplementServices.TarifVenteServiceImpl;

import java.util.List;


@RestController
@RequestMapping("/api/tarifs-vente")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TarifVenteController {

   private final TarifVenteServiceImpl tarifVenteService;

    /* =========================================================
       TARIFS DE VENTE
       ========================================================= */

    @PostMapping
    public TarifVenteResponse create(@RequestBody TarifVenteRequest request) {
        return tarifVenteService.createTarif(request);
    }

    @PutMapping("/{id}")
    public TarifVenteResponse update(@PathVariable Long id, @RequestBody TarifVenteRequest request) {
        return tarifVenteService.updateTarif(id, request);
    }

    @GetMapping
    public List<TarifVenteResponse> getAll() {
        return tarifVenteService.getAll();
    }

    @GetMapping("/actifs")
    public List<TarifVenteResponse> getAllActifs() {
        return tarifVenteService.getAllActifs();
    }

    @PutMapping("/toggle-actif/{id}")
    public TarifVenteResponse toggleActif(@PathVariable Long id) {
        return tarifVenteService.toggleActif(id);
    }


    @PutMapping("/defaut/{id}")
    public TarifVenteResponse definirParDefaut(@PathVariable Long id) {
        return tarifVenteService.definirParDefaut(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        tarifVenteService.deleteTarif(id);
    }

    /* =========================================================
       REGLES TARIFAIRES PAR CATEGORIE
       ========================================================= */

    @PostMapping("/regles")
    public TarifCategorieProduitResponse createOrUpdateRegle(@RequestBody TarifCategorieProduitRequest request) {

        return tarifVenteService.createOrUpdateRegle(request);
    }

    @GetMapping("/regles")
    public List<TarifCategorieProduitResponse> getAllRegles() {
        return tarifVenteService.getAllRegles();
    }

    @GetMapping("/{tarifVenteId}/regles")
    public List<TarifCategorieProduitResponse> getReglesByTarif(@PathVariable Long tarifVenteId) {
        return tarifVenteService.getReglesByTarif(tarifVenteId);
    }

    /* =========================================================
       CALCUL TARIFAIRE
       ========================================================= */

    @PostMapping("/calcul")
    public TarificationResponse calculerPrix(@RequestBody TarificationProduitRequest request) {
        return tarifVenteService.calculerPrix(request);
    }

    @PostMapping("/calcul-lot")
    public List<TarificationResponse> calculerPrixLot(@RequestBody TarificationLotRequest request) {
        return tarifVenteService.calculerPrixLot(request);
    }
}
