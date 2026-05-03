package com.king.pos.Web;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.king.pos.Dto.AnnulationVenteRequest;
import com.king.pos.Dto.RapportVenteFilterRequest;
import com.king.pos.Dto.RapportVentePosResponse;
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

            for (var ligne : request.getLignes()) {
                    System.out.println("Ligne Vente - Produit ID: " + ligne.getProduitId() + ", Quantité: " + ligne.getQuantite() + ", Prix Unitaire: " + ligne.getPrixCDF() + ", Remise: " + ligne.getRemise() + ", Taux "+ ligne.getTauxChange() + ", Total: " + ligne.getTotal());
            }
        
        return venteService.enregistrerVente(request);
    }


    @PostMapping("/rapports")
public RapportVentePosResponse getRapportVentes(
        @RequestBody RapportVenteFilterRequest filter) {
    return venteService.genererRapportVentes(filter);
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
