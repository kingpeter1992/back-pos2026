package com.king.pos.Web;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.king.pos.Dto.AnnulationVenteRequest;
import com.king.pos.Dto.VenteRequest;
import com.king.pos.Dto.Response.ProduitPosResponse;
import com.king.pos.Dto.Response.VenteResponse;
import com.king.pos.ImplementServices.VenteServiceImpl;
import com.king.pos.Interface.StockService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ventes")
@RequiredArgsConstructor
@CrossOrigin("*")
public class VenteController {

    private final VenteServiceImpl venteService;
    private final StockService stockService;

    
    @PostMapping
    public VenteResponse enregistrer(@RequestBody VenteRequest request) {
        return venteService.enregistrerVente(request);
    }
@PostMapping("/{id}/annuler")
public VenteResponse annulerVente(
        @PathVariable Long id,
        @RequestBody @Valid AnnulationVenteRequest request
) {
    return venteService.annulerVente(id, request);
}


    @GetMapping
    public List<VenteResponse> getAllVente() {
        return venteService.getAllVente();
    }

    @GetMapping("/pos")
    public List<ProduitPosResponse> getProduitsPos() {
        return stockService.getProduitsPos();
    }
}
